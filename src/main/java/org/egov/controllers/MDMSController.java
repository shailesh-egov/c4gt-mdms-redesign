package org.egov.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.models.*;
import org.egov.service.JSONValidationService;
import org.egov.service.MDMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@RestController
@Slf4j
@RequestMapping("/mdms/v1")
public class MDMSController {
    @Autowired
    private MDMSService service;

    @Autowired
    private JSONValidationService validationService;

    private MDMSResponse buildResponse(String message,ArrayList<MDMSData> masterDataBody){
        MDMSResponse response = new MDMSResponse();
        response.setMessage(message);
        response.setMasterData(masterDataBody);

        return response;
    }
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> createMasterData(@RequestBody MDMSNewRequest request) {
        ArrayList<MDMSData> responseBody = new ArrayList<>();
        String message;

//        try {
////            validationService.validateMasterDataSchema(request.getMasterName(), request.getMasterData());
//        }
//        catch(Exception e) {
//            message = "Invalid request body: " + e.getMessage();
//            MDMSResponse response = buildResponse(message,responseBody);
//
//            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
//        }

        responseBody.add(service.saveMDMSData(request));
        message = "Creation successful";

        MDMSResponse response = buildResponse(message,responseBody);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> getMasterData(){
        ArrayList<MDMSData> responseBody = new ArrayList<>();
        String message;

        try {
            responseBody = (ArrayList<MDMSData>) service.getMDMSData();
        }catch (Exception e){
            message = e.getMessage();
            MDMSResponse response = buildResponse(message,responseBody);

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        message = "All master data fetched";
        MDMSResponse response = buildResponse(message,responseBody);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search/{id}", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> getMasterDataById(@PathVariable int id){
        ArrayList<MDMSData> responseBody = new ArrayList<>();
        String message;
        try{
            MDMSData result = service.getMDMSDataById(id);

            if (result == null) {
                message = "Object not found by id";
                MDMSResponse response = buildResponse(message,responseBody);
                return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
            }

            message = "Object found by id";
            responseBody.add(result);
            MDMSResponse response = buildResponse(message,responseBody);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e){
            message = e.getMessage();
            MDMSResponse response = buildResponse(message,responseBody);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/_search/by_master_name/{masterName}", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> getMasterDataById(@PathVariable String masterName){
        ArrayList<MDMSData> responseBody = new ArrayList<>();
        String message;
        try{
            MDMSData result = service.getMDMSDataByMasterName(masterName);

            if (result == null) {
                message = "Object not found by master name";
                MDMSResponse response = buildResponse(message,responseBody);

                return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
            }


            message = "Object found by master name";
            responseBody.add(result);
            MDMSResponse response = buildResponse(message,responseBody);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e){
            message = e.getMessage();
            MDMSResponse response = buildResponse(message,responseBody);

            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //returns a specific object inside master data based on a search key and  search value
    //example for master:"department" key:"name" value:"Tax" will return this object
    //    {
    //        "name": "Tax",
    //            "code": "REV",
    //            "active": true
    //    },
    //Check the below link for department master data
    //https://github.com/egovernments/works-mdms-data/blob/DEV/data/pg/common-masters/Department.json
    @RequestMapping(value = "/_search/byKey", method = RequestMethod.POST)
    public ResponseEntity<MDMSSearchResponse> getMasterDataObjectBySearchKey(@RequestBody SearchRequestBody requestBody){
        MDMSSearchResponse response = new MDMSSearchResponse();

        JsonNode responseBody = null;
        String message;

        try{
            responseBody = service.getMDMSDataObjectBySearchKey(requestBody.getMasterName(),requestBody.getSearchKey(),requestBody.getSearchValue());
        }
        catch (Exception e){
            message = e.getMessage();
            response.setMessage(message);
            log.info("message: ",message);
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
        message = "Master data object found by given search key-value pair";
        response.setMessage(message);
        response.setMasterData(responseBody);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> updateMasterData(@RequestBody MDMSRequest request){
        ArrayList<MDMSData> responseBody = new ArrayList<>();
        String message;
        try {
            validationService.validateMasterDataSchema(request.getMasterName(), request.getMasterData());
        }
        catch(Exception e) {
            message = "Invalid request body: " + e.getMessage();
            MDMSResponse response = buildResponse(message,responseBody);

            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        try {
            responseBody.add(service.updateMDMSData(request));
        }catch (Exception e){
            message = e.getMessage();
            MDMSResponse response = buildResponse(message,responseBody);

            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        message = "Master data updated";
        MDMSResponse response = buildResponse(message,responseBody);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/_update/byKey", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> updateMasterDataObjectBySearchKey(@RequestBody UpdateRequestBody requestBody){
        ArrayList<MDMSData> responseBody = new ArrayList<>();
        String message;

        try{
            responseBody.add(service.updateMDMSDataObjectBySearchKey(requestBody.getMasterName(),requestBody.getSearchKey(),requestBody.getSearchValue(),requestBody.getUpdateKey(),requestBody.getUpdateValue()));
        } catch(Exception e){
            message = e.getMessage();
            MDMSResponse response = buildResponse(message,responseBody);

            return new ResponseEntity<>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        message = "Master Data Object updated successfully";
        MDMSResponse response = buildResponse(message,responseBody);

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @RequestMapping(value = "/_delete/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> deleteMasterData(@PathVariable int id){

        try{
            String message = "Master data deleted "+service.deleteMDMSData(id);
            return new ResponseEntity<>(message, HttpStatus.OK);
        }
        catch (Exception e) {
            String message = "Master data ID not present";
            return new ResponseEntity<>(message,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
