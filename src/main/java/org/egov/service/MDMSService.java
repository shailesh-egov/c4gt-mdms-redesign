package org.egov.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import digit.models.coremodels.mdms.MasterDetail;
import digit.models.coremodels.mdms.MdmsCriteriaReq;
import digit.models.coremodels.mdms.ModuleDetail;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONValue;
import org.egov.config.MDMSConfig;
import org.egov.kafka.Producer;
import org.egov.models.MDMSData;
import org.egov.models.MDMSRequest;
import org.egov.repository.MDMSRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import net.minidev.json.JSONArray;
//import org.egov.common.contract.response.ResponseInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.egov.works.util.ResponseInfoFactory;
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

        Map<String, Map<String, JSONArray>> stateLevel = null;
        Map<String, Map<String, JSONArray>> ulbLevel = null;

        if (tenantId.contains(".")) {
            String array[] = tenantId.split("\\.");

            //repalce both lines with DB call
//            stateLevel = tenantIdMap.get(array[0]);
//            ulbLevel = tenantIdMap.get(tenantId);
            if (ulbLevel == null)
                throw new CustomException("Invalid_tenantId.MdmsCriteria.tenantId", "Invalid Tenant Id");
        } else {
            //repalce both lines with DB call
//            stateLevel = tenantIdMap.get(tenantId);
            if (stateLevel == null)
                throw new CustomException("Invalid_tenantId.MdmsCriteria.tenantId", "Invalid Tenant Id");
        }

        List<ModuleDetail> moduleDetails = mdmsCriteriaReq.getMdmsCriteria().getModuleDetails();
        Map<String, Map<String, JSONArray>> responseMap = new HashMap<>();

        for (ModuleDetail moduleDetail : moduleDetails) {
            List<MasterDetail> masterDetails = moduleDetail.getMasterDetails();

            //This will taken care once above code works
            if (stateLevel != null || ulbLevel != null) {
                if (stateLevel != null && ulbLevel == null) {
                    if (stateLevel.get(moduleDetail.getModuleName()) == null)
                        continue;
                } else if (ulbLevel != null && stateLevel == null) {
                    if (ulbLevel.get(moduleDetail.getModuleName()) == null)
                        continue;
                }
                if (stateLevel != null || ulbLevel != null) {
                    if (stateLevel.get(moduleDetail.getModuleName()) == null
                            && ulbLevel.get(moduleDetail.getModuleName()) == null)
                        continue;
                }
            }

            Map<String, JSONArray> finalMasterMap = new HashMap<>();

            for (MasterDetail masterDetail : masterDetails) {
                JSONArray masterData = null;

                try {
                    //DB call
                    masterData = getMasterData(tenantId, moduleDetail.getModuleName(), masterDetail.getName());
                } catch (Exception e) {
                    log.error("Exception occurred while reading master data", e);
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


    //Should return an array of master data
    //Now only returns one object
    private JSONArray getMasterData(String tenantId, String moduleName, String masterName) {

        //Fetching master data object from database
        MDMSData data = repository.findByTenantIdAndModuleNameAndMasterName(tenantId,moduleName,masterName);

        if(data == null){
            throw new RuntimeException("Master Data not present");
        }
        //Fetching the master data from the object
        JsonNode masterData = data.getMasterData();

        //Converting JSON-node to JSONArray for backward compatibility
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
    public MDMSData saveMDMSData(MDMSRequest request) {

        try{
            producer.push(config.getSaveMDMDSDataTopic(),request);
        }catch (Exception e){
            throw new RuntimeException("Error while creating master data "+e.getMessage());
        }

        return new MDMSData();
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSData updateMDMSData(MDMSRequest request) {
        try {
            producer.push(config.getUpdateMDMDSDataTopic(),request);
        } catch (Exception e) {
            log.error("Error while updating MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while updating MDMSData", e);
        }

        return new MDMSData();
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
