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
import org.egov.models.MasterConfig;
import org.egov.repository.ConfigRepository;
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

    @Autowired
    private ConfigRepository masterConfigRepository;

    // For creating fallback functionality
    public MasterConfig createMasterConfigData(MasterConfig request) {
        return masterConfigRepository.save(request);
    }

    public Map<String, Map<String, JSONArray>> searchMaster(MdmsCriteriaReq mdmsCriteriaReq) {
        String tenantId = mdmsCriteriaReq.getMdmsCriteria().getTenantId();

        String stateLevelTenantId = null;
        String ulbLevelTenantId  = null;

        List<MDMSData> stateLevel = null;
        List<MDMSData> ulbLevel = null;

        if (tenantId.contains(".")) {
            String array[] = tenantId.split("\\.");

            stateLevelTenantId = array[0];
            ulbLevelTenantId = array[1];

            ulbLevel = repository.findAllByTenantId(ulbLevelTenantId);
            if (ulbLevel == null)
                throw new CustomException("Invalid_tenantId.MdmsCriteria.tenantId", "Invalid Tenant Id");
        } else {
            stateLevelTenantId = tenantId;
            stateLevel = repository.findAllByTenantId(stateLevelTenantId);
            if ( stateLevel == null)
                throw new CustomException("Invalid_tenantId.MdmsCriteria.tenantId", "Invalid Tenant Id");
        }


        List<ModuleDetail> moduleDetails = mdmsCriteriaReq.getMdmsCriteria().getModuleDetails();
        Map<String, Map<String, JSONArray>> responseMap = new HashMap<>();

        for (ModuleDetail moduleDetail : moduleDetails) {
            List<MasterDetail> masterDetails = moduleDetail.getMasterDetails();


             if (stateLevel != null || ulbLevel != null) {
                 List<MDMSData> stateLevelModules = null;
                 List<MDMSData> ulbLevelModules = null;

                 if (stateLevel != null && ulbLevel == null) {
                     stateLevelModules = repository.findAllByTenantIdAndModuleName(stateLevelTenantId,moduleDetail.getModuleName());

                     if(stateLevelModules == null)
                        continue;
                 }
                 else if (ulbLevel != null && stateLevel == null) {

                     ulbLevelModules = repository.findAllByTenantIdAndModuleName(ulbLevelTenantId,moduleDetail.getModuleName());

                    if (ulbLevelModules == null)
                        continue;
                }
                 if (stateLevel != null || ulbLevel != null) {
                     if (stateLevelModules == null && ulbLevelModules == null)
                        continue;
                 }
             }

            Map<String, JSONArray> finalMasterMap = new HashMap<>();

            for (MasterDetail masterDetail : masterDetails) {
                JSONArray masterData = null;

                try {
                    masterData = getMasterData(stateLevelTenantId,ulbLevelTenantId,stateLevel,ulbLevel,tenantId, moduleDetail.getModuleName(), masterDetail.getName());
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

    // What should this return an array of master data or simply one master data object
    // Currently returns only one master data object
    private JSONArray getMasterData(String stateLevelTenantId,String ulbLevelTenantId,List<MDMSData> stateLevel,List<MDMSData> ulbLevel,String tenantId, String moduleName, String masterName) {

        //Fallback functionality
        List<MasterConfig> moduleMetaData = masterConfigRepository.findAllByModuleName(moduleName);
        MasterConfig masterMetaData = null;

        Boolean isStateLevel = false;

        if (moduleMetaData != null)
            masterMetaData = masterConfigRepository.findByModuleNameAndMasterName(moduleName,masterName);


        if (null != masterMetaData) {
            try{
                isStateLevel = masterMetaData.getIsStateLevel();
            }catch (Exception e) {
                log.error("Error while determining state level, falling back to false state.");
                isStateLevel = false;
            }

        }

        // Fetching master data object from database
        MDMSData masterDataObject;
        log.info("MasterName... " + masterName + "isStateLevelConfiguration.." + isStateLevel);

        // Master Data fetching along with fallback
        if (ulbLevel == null || isStateLevel) {
            if (repository.findAllByTenantIdAndModuleName(stateLevelTenantId,moduleName) != null) {

                masterDataObject = repository.findByTenantIdAndModuleNameAndMasterName(tenantId, moduleName, masterName);
                JSONArray masterData = convertMasterDataObjectToJsonArray(masterDataObject);

                return masterData;
            } else {
                return null;
            }
        } else if (ulbLevel != null && repository.findAllByTenantIdAndModuleName(ulbLevelTenantId,moduleName) != null) {
            // Fallback call same as old
            masterDataObject = repository.findByTenantIdAndModuleNameAndMasterName(ulbLevelTenantId, moduleName, masterName);
            JSONArray masterData = convertMasterDataObjectToJsonArray(masterDataObject);

            return masterData;
        } else {
            return null;
        }
    }

    public JSONArray filterMaster(JSONArray masters, String filterExp) {
        JSONArray filteredMasters = JsonPath.read(masters, filterExp);
        return filteredMasters;
    }

    private JSONArray convertMasterDataObjectToJsonArray(MDMSData masterDataObject){
        JsonNode masterData = masterDataObject.getMasterData();

        // Converting JSON-node to JSONArray for backward compatibility
        String jsonString = masterData.toString();
        Object parsedObject = JSONValue.parse(jsonString);

        if (parsedObject instanceof JSONArray) {
            return (JSONArray) parsedObject;
        } else {
            throw new IllegalArgumentException("The parsed JSON is not an array.");
        }
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSData saveMDMSData(MDMSRequest request) {

        try {
            producer.push(config.getSaveMDMDSDataTopic(), request);
        } catch (Exception e) {
            throw new RuntimeException("Error while creating master data " + e.getMessage());
        }

        return new MDMSData();
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSData updateMDMSData(MDMSRequest request) {
        try {
            producer.push(config.getUpdateMDMDSDataTopic(), request);
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
