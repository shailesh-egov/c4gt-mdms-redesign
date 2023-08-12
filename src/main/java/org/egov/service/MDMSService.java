package org.egov.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.models.MDMSData;
import org.egov.models.MDMSRequest;
import org.egov.repository.MDMSRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MDMSService {

    @Autowired
    private MDMSRepository repository;

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSData saveMDMSData(MDMSRequest request) {

        try {
            //Appending master data to existing data using master name
            String masterName = request.getMasterName();

            //Fetching existing master data Object
            MDMSData existingMasterDataObject = getMDMSDataByMasterName(masterName);

            // If no master data is present create a new one
            if (existingMasterDataObject == null) {
                MDMSData object = new MDMSData();

                object.setTenantId(request.getTenantId());
                object.setModuleName(request.getModuleName());
                object.setMasterName(request.getMasterName());
                object.setMasterData(request.getMasterData());

                return repository.save(object);
            }

            //Fetching new master data to be appended
            JsonNode newMasterDataObject = request.getMasterData();

            //Fetching existing master data
            JsonNode existingMasterData = existingMasterDataObject.getMasterData();

            if (existingMasterData.isArray() && newMasterDataObject.isArray()) {

                // Casting the existing master data to ArrayNode (assuming it is an array)
                ArrayNode existingMasterDataArrayNode = (ArrayNode) existingMasterData;

                //Looping through new master data
                for (JsonNode element : newMasterDataObject) {
                    // //Appending the object to existing master data
                    existingMasterDataArrayNode.add(element);
                }

                //Setting the master data in MDMS request body
                existingMasterDataObject.setMasterData(existingMasterDataArrayNode);
                log.info("existingMasterDataObject: " + existingMasterDataObject);
            }
            return repository.save(existingMasterDataObject);
        }
        catch (Exception e) {
            log.error("Error while saving MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while saving MDMSData", e);
        }
    }

    @Cacheable(value = "mdmsDataCache")
    public List<MDMSData> getMDMSData() {

        try{
            return repository.findAll();
        }catch (Exception e) {
            log.error("Error while fetching MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while fetching MDMSData", e);
        }

    }

    @Cacheable( value = "mdmsDataCache",key = "#id")
    public MDMSData getMDMSDataById(int id) {
        try {
            return repository.findById(id).orElse(null);
        }
        catch (Exception e) {
            log.error("Error while fetching MDMSData by ID: " + e.getMessage(), e);
            throw new RuntimeException("Error while fetching MDMSData by ID", e);
        }
    }

    @Cacheable(value = "mdmsDataCache",key = "#masterName")
    public MDMSData getMDMSDataByMasterName(String masterName){

        try {
            return repository.findByMasterName(masterName);
        }
        catch (Exception e) {
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
            }catch (Exception e) {
                log.error("Error while finding MDMSData for updating: " + e.getMessage(), e);
                throw new RuntimeException("Error while finding MDMSData for updating", e);
            }

            try {
                existingData.setTenantId(request.getTenantId());
            }catch (Exception e) {
                log.error("Error while finding MDMSData for updating: " + e.getMessage(), e);
                throw new RuntimeException("Error while finding MDMSData for updating", e);
            }

            existingData.setModuleName(request.getModuleName());
            existingData.setMasterName(request.getMasterName());
            existingData.setMasterData(request.getMasterData());

            return repository.save(existingData);
        }
        catch (Exception e) {
            log.error("Error while updating MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while updating MDMSData", e);
        }
    }

    @CacheEvict( value = "mdmsDataCache",allEntries = true)
    public String deleteMDMSData(int id) {

        try {
            repository.deleteById(id);
            return "Request removed !! " + id;
        }
         catch (Exception e) {
            log.error("Error while deleting MDMSData: " + e.getMessage(), e);
            throw new RuntimeException("Error while deleting MDMSData", e);
        }
    }
    public JsonNode getMDMSDataObjectBySearchKey(String masterName,String searchKey,String searchValue){
        //search-key could be anything as per the master schema
        //Here for department we are using "code" as key for searching

        MDMSData existingMasterDataObject = getMDMSDataByMasterName(masterName);
        if (existingMasterDataObject == null){
            throw new RuntimeException("No master data found by master name");
        }

        JsonNode existingMasterData = existingMasterDataObject.getMasterData();
        JsonNode requiredMasterData = null;

        for (JsonNode masterDataElement : existingMasterData) {
            if( masterDataElement.has(searchKey )&& (masterDataElement.get(searchKey).asText()).equals(searchValue)){
                requiredMasterData = masterDataElement;
                break;
            }
        }
        if(requiredMasterData == null){
            throw new RuntimeException("No master data object found by search key-value pair");
        }
        return requiredMasterData;
    }

    public MDMSData updateMDMSDataObjectBySearchKey(String masterName,String searchKey,String searchValue,String updateKey,String updateValue){
        MDMSData existingMasterDataObject = getMDMSDataByMasterName(masterName);
        if (existingMasterDataObject == null){
            throw new RuntimeException("No master data found by master name");
        }

        JsonNode existingMasterData = existingMasterDataObject.getMasterData();
        boolean updateFlag = false;
        for (JsonNode masterDataElement : existingMasterData) {
            if((masterDataElement.get(searchKey).asText()).equals(searchValue)){
                if (masterDataElement instanceof ObjectNode) {
                    // Update the value of the specified updateKey
                    ((ObjectNode) masterDataElement).put(updateKey, updateValue);
                    updateFlag = true;
                    break;
                }
            }
        }

        if(!updateFlag){
            throw new RuntimeException("No master data object found by search key-value pair");
        }
        return existingMasterDataObject;
    }
}
