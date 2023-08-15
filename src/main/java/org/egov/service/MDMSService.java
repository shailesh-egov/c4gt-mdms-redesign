package org.egov.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import digit.models.coremodels.mdms.MasterDetail;
import digit.models.coremodels.mdms.MdmsCriteriaReq;
import digit.models.coremodels.mdms.ModuleDetail;
import lombok.extern.slf4j.Slf4j;
import org.egov.models.MDMSData;
import org.egov.models.MDMSNewRequest;
import org.egov.models.MDMSRequest;
import org.egov.repository.MDMSRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import net.minidev.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MDMSService {

    @Autowired
    private MDMSRepository repository;

    @Autowired
    private Producer producer;

    @Autowired
    private MDMSConfig config;

    public Map<String, Map<String, JSONArray>> searchMaster(MdmsCriteriaReq mdmsCriteriaReq) {
        String tenantId = mdmsCriteriaReq.getMdmsCriteria().getTenantId();
        log.info("tenantId" + tenantId);

        List<ModuleDetail> moduleDetails = mdmsCriteriaReq.getMdmsCriteria().getModuleDetails();
        log.info("moduleDetails" + moduleDetails);

        Map<String, Map<String, JSONArray>> responseMap = new HashMap<>();

        for (ModuleDetail moduleDetail : moduleDetails) {
            List<MasterDetail> masterDetails = moduleDetail.getMasterDetails();
            log.info("masterDetails" + masterDetails);

            Map<String, JSONArray> finalMasterMap = new HashMap<>();

            for (MasterDetail masterDetail : masterDetails) {
                JSONArray masterData = null;

                try {
                    // DB call
                    masterData = getMasterDataFromDatabase(tenantId, moduleDetail.getModuleName(),
                            masterDetail.getName());
                } catch (Exception e) {
                    throw new RuntimeException("Exception occurred while reading master data", e);
                }

                if (masterData == null)
                    continue;

                if (masterDetail.getFilter() != null)
                    masterData = filterMaster(masterData, masterDetail.getFilter());

                finalMasterMap.put(masterDetail.getName(), masterData);
            }
            responseMap.put(moduleDetail.getModuleName(), finalMasterMap);
        }
        return responseMap;
    }

    private JSONArray getMasterDataFromDatabase(String tenantId, String moduleName, String masterName) {

        // Fetching master data object from database
        MDMSData data = repository.findByTenantIdAndModuleNameAndMasterName(tenantId, moduleName, masterName);

        // Fetching the master data from the object
        JsonNode masterData = data.getMasterData();

        // Converting JSON-node to JSONArray for backward compatibility
        String jsonString = masterData.toString();
        Object parsedObject = JSONValue.parse(jsonString);

        if (parsedObject instanceof JSONArray) {
            return (JSONArray) parsedObject;
        } else {
            throw new IllegalArgumentException("The parsed JSON is not an array.");
        }
    }

    public JSONArray filterMaster(JSONArray masters, String filterExp) {
        JSONArray filteredMasters = JsonPath.read(masters, filterExp);
        return filteredMasters;
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSData saveMDMSData(MDMSNewRequest newRequest) {
        MDMSData request = newRequest.getMdmsData();
        try {
            // Appending master data to existing data using master name
            String masterName = request.getMasterName();

            // Fetching existing master data Object
            MDMSData existingMasterDataObject = getMDMSDataByMasterName(masterName);

            // If no master data is present create a new one
            if (existingMasterDataObject == null) {
                MDMSData object = new MDMSData();

                object.setTenantId(request.getTenantId());
                object.setModuleName(request.getModuleName());
                object.setMasterName(request.getMasterName());
                object.setMasterData(request.getMasterData());

                producer.push(config.getSaveMDMDSDataTopic(), newRequest);
                // return repository.save(object);
            }

            // Fetching new master data to be appended
            JsonNode newMasterDataObject = request.getMasterData();

            // Fetching existing master data
            JsonNode existingMasterData = existingMasterDataObject.getMasterData();

            if (existingMasterData.isArray() && newMasterDataObject.isArray()) {

                // Casting the existing master data to ArrayNode (assuming it is an array)
                ArrayNode existingMasterDataArrayNode = (ArrayNode) existingMasterData;

                // Looping through new master data
                for (JsonNode element : newMasterDataObject) {
                    // //Appending the object to existing master data
                    existingMasterDataArrayNode.add(element);
                }

                // Setting the master data in MDMS request body
                existingMasterDataObject.setMasterData(existingMasterDataArrayNode);
                log.info("existingMasterDataObject: " + existingMasterDataObject);
            }
            return repository.save(existingMasterDataObject);
        } catch (Exception e) {
            log.error("Error while saving MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while saving MDMSData", e);
        }
    }

    @Cacheable(value = "mdmsDataCache")
    public List<MDMSData> getMDMSData() {

        try {
            return repository.findAll();
        } catch (Exception e) {
            log.error("Error while fetching MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while fetching MDMSData", e);
        }

    }

    @Cacheable(value = "mdmsDataCache", key = "#id")
    public MDMSData getMDMSDataById(int id) {
        try {
            return repository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("Error while fetching MDMSData by ID: " + e.getMessage(), e);
            throw new RuntimeException("Error while fetching MDMSData by ID", e);
        }
    }

    @Cacheable(value = "mdmsDataCache", key = "#masterName")
    public MDMSData getMDMSDataByMasterName(String masterName) {

        try {
            return repository.findByMasterName(masterName);
        } catch (Exception e) {
            log.error("Error while fetching MDMSData by Master Name: " + e.getMessage(), e);
            throw new RuntimeException("Error while fetching MDMSData by Master Name", e);
        }
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSData updateMDMSData(MDMSRequest request) {

        try {
            MDMSData existingData;
            try {
                existingData = repository.findById(request.getId()).orElse(null);
            } catch (Exception e) {
                log.error("Error while finding MDMSData for updating: " + e.getMessage(), e);
                throw new RuntimeException("Error while finding MDMSData for updating", e);
            }

            try {
                existingData.setTenantId(request.getTenantId());
            } catch (Exception e) {
                log.error("Error while finding MDMSData for updating: " + e.getMessage(), e);
                throw new RuntimeException("Error while finding MDMSData for updating", e);
            }

            existingData.setModuleName(request.getModuleName());
            existingData.setMasterName(request.getMasterName());
            existingData.setMasterData(request.getMasterData());

            return repository.save(existingData);
        } catch (Exception e) {
            log.error("Error while updating MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while updating MDMSData", e);
        }
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public String deleteMDMSData(int id) {

        try {
            repository.deleteById(id);
            return "Request removed !! " + id;
        } catch (Exception e) {
            log.error("Error while deleting MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while deleting MDMSData", e);
        }
    }
}
