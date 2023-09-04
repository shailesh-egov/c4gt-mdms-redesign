package org.egov.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import digit.models.coremodels.mdms.MasterDetail;
import digit.models.coremodels.mdms.MdmsCriteriaReq;
import digit.models.coremodels.mdms.ModuleDetail;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.config.MDMSConfig;
import org.egov.kafka.Producer;
import org.egov.models.*;
import org.egov.repository.ConfigRepository;
import org.egov.repository.MDMSRepository;
import org.egov.tracer.model.CustomException;
import org.egov.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MDMSService {


    @Autowired
    private Producer producer;

    @Autowired
    private MDMSConfig config;

    @Autowired
    private MDMSRepository repository;

    @Autowired
    private ConfigRepository masterConfigRepository;
    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @Cacheable(value = "mdmsDataCache", key = "#mdmsCriteriaReq.MdmsCriteria.tenantId"+"."+"#mdmsCriteriaReq.MdmsCriteria.moduleDetails.toString()")
    public Map<String, Map<String, JSONArray>> searchMaster(MdmsCriteriaReq mdmsCriteriaReq) {
        String tenantId = mdmsCriteriaReq.getMdmsCriteria().getTenantId();

        String stateLevelTenantId;
        String ulbLevelTenantId = null;

        boolean stateLevel = false;
        boolean ulbLevel = false;

        // Checking if data is present in tenant level
        if (tenantId.contains(".")) {
            String[] array = tenantId.split("\\.");

            stateLevelTenantId = array[0];
            ulbLevelTenantId = array[1];

            ulbLevel = repository.existsByTenantId(ulbLevelTenantId);
            if (!ulbLevel) throw new CustomException("Invalid_tenantId.MdmsCriteria.tenantId", "Invalid Tenant Id");
        } else {
            stateLevelTenantId = tenantId;

            stateLevel = repository.existsByTenantId(stateLevelTenantId);
            if (!stateLevel) throw new CustomException("Invalid_tenantId.MdmsCriteria.tenantId", "Invalid Tenant Id");
        }

        List<ModuleDetail> moduleDetails = mdmsCriteriaReq.getMdmsCriteria().getModuleDetails();
        Map<String, Map<String, JSONArray>> responseMap = new HashMap<>();

        for (ModuleDetail moduleDetail : moduleDetails) {
            List<MasterDetail> masterDetails = moduleDetail.getMasterDetails();

            boolean stateLevelModules;
            boolean ulbLevelModules;

            if (stateLevel) {
                stateLevelModules = repository.existsByTenantIdAndModuleName(stateLevelTenantId, moduleDetail.getModuleName());

                if (!stateLevelModules) continue;
            } else {
                ulbLevelModules = repository.existsByTenantIdAndModuleName(ulbLevelTenantId, moduleDetail.getModuleName());

                if (!ulbLevelModules) continue;
            }

            Map<String, JSONArray> finalMasterMap = new HashMap<>();

            for (MasterDetail masterDetail : masterDetails) {
                JSONArray masterData = null;

                try {
                    masterData = getMasterData(stateLevelTenantId, ulbLevelTenantId, ulbLevel, moduleDetail.getModuleName(), masterDetail.getName());
                } catch (Exception e) {
                    log.error("Exception occurred while reading master data", e);
                }

                if (masterData == null) continue;

                if (masterDetail.getFilter() != null) masterData = filterMaster(masterData, masterDetail.getFilter());

                finalMasterMap.put(masterDetail.getName(), masterData);
            }
            responseMap.put(moduleDetail.getModuleName(), finalMasterMap);

        }

        return responseMap;
    }

    private JSONArray getMasterData(String stateLevelTenantId, String ulbLevelTenantId, boolean ulbLevel, String moduleName, String masterName) {

        // Fallback functionality
        boolean moduleMetaData = masterConfigRepository.existsByModuleName(moduleName);
        MasterConfig masterMetaData = null;

        Boolean isStateLevel = false;

        if (moduleMetaData)
            masterMetaData = masterConfigRepository.findByModuleNameAndMasterName(moduleName, masterName);

        if (null != masterMetaData) {
            try {
                isStateLevel = masterMetaData.getIsStateLevel();
            } catch (Exception e) {
                log.error("Error while determining state level, falling back to false state.");
                isStateLevel = false;
            }

        }

        // Fetching master data object from database
        MDMSData masterDataObject;
        log.info("MasterName... " + masterName + "isStateLevelConfiguration.." + isStateLevel);

        // Checking if data is present in tenant level and module level
        // Master Data fetching along with fallback
        if (!ulbLevel || isStateLevel) {

            if (repository.existsByTenantIdAndModuleName(stateLevelTenantId, moduleName)) {

                masterDataObject = repository.findByTenantIdAndModuleNameAndMasterName(stateLevelTenantId, moduleName, masterName);
                return convertMasterDataObjectToJsonArray(masterDataObject);
            } else {
                return null;
            }

        } else if (repository.existsByTenantIdAndModuleName(ulbLevelTenantId, moduleName)) {
            masterDataObject = repository.findByTenantIdAndModuleNameAndMasterName(ulbLevelTenantId, moduleName, masterName);
            return convertMasterDataObjectToJsonArray(masterDataObject);
        } else {
            return null;
        }
    }

    public JSONArray filterMaster(JSONArray masters, String filterExp) {
        return JsonPath.read(masters, filterExp);
    }

    private JSONArray convertMasterDataObjectToJsonArray(MDMSData masterDataObject) {
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
    public MDMSResponse createMDMSData(MDMSRequest mdmsRequest) {

        producer.push(config.getSaveMDMDSDataTopic(), mdmsRequest);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(mdmsRequest.getRequestInfo(), true);
        MDMSResponse mdmsResponse = MDMSResponse.builder().responseInfo(responseInfo).masterData(Collections.singletonList(mdmsRequest.getMdmsData())).build();
        return mdmsResponse;
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSResponse updateMDMSData(MDMSRequest mdmsRequest) {

        producer.push(config.getUpdateMDMDSDataTopic(), mdmsRequest);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(mdmsRequest.getRequestInfo(), true);
        MDMSResponse mdmsResponse = MDMSResponse.builder().responseInfo(responseInfo).masterData(Collections.singletonList(mdmsRequest.getMdmsData())).build();

        return mdmsResponse;
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public String deleteMDMSData(int id) {

        try {
            // repository.deleteById(id);
            return "Request removed !! " + id;
        } catch (Exception e) {
            log.error("Error while deleting MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while deleting MDMSData", e);
        }
    }

    // For fallback functionality
    public MasterConfigRequest createMasterConfigData(MasterConfigRequest request) {
        producer.push(config.getSaveMDMDSConfigTopic(), request);
        return request;
    }

    public MasterConfigRequest updateMasterConfigData(MasterConfigRequest request) {
        producer.push(config.getUpdateMDMDSConfigTopic(), request);
        return request;
    }
}

