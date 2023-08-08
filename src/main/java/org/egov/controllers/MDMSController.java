package org.egov.controllers;


import org.egov.models.MDMSData;
import org.egov.models.MDMSRequest;
import org.egov.models.MDMSResponse;
import org.egov.service.JSONValidationService;
import org.egov.service.MDMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;


@Controller
@RequestMapping("/mdms/v1")
public class MDMSController {
    @Autowired
    private MDMSService service;

    @Autowired
    private JSONValidationService validationService;
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> createMasterData(@RequestBody MDMSRequest request) {

        MDMSResponse response = new MDMSResponse();

        try {
            validationService.validateMasterDataSchema(request.getMasterName(), request.getMasterData());
        }
        catch(Exception e) {
            String validationErrorMessage = "Invalid request body: " + e.getMessage();
            response.setMessage(validationErrorMessage);

            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        ArrayList<MDMSData> responseBody = new ArrayList<>();
        responseBody.add(service.saveMDMSData(request));

        response.setMasterData(responseBody);
        response.setMessage("Creation successful");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> getMasterData(){

        MDMSResponse response = new MDMSResponse();

        try {
            response.setMasterData((ArrayList<MDMSData>) service.getMDMSData());
        }catch (Exception e){
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.setMessage("All master data fetched");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/_search/{id}", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> getMasterDataById(@PathVariable int id){
        MDMSResponse response = new MDMSResponse();
        try{
            MDMSData result = service.getMDMSDataById(id);

            if (result == null) {
                response.setMessage("Object not found by id");
                return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
            }
            ArrayList<MDMSData> responseBody = new ArrayList<>();

            response.setMessage("Object found by id");

            responseBody.add(result);
            response.setMasterData(responseBody);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e){
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/_search/by/{masterName}", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> getMasterDataById(@PathVariable String masterName){
        MDMSResponse response = new MDMSResponse();
        try{
            MDMSData result = service.getMDMSDataByMasterName(masterName);

            if (result == null) {
                response.setMessage("Object not found by master name");
                return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
            }
            ArrayList<MDMSData> responseBody = new ArrayList<>();

            response.setMessage("Object found by master name");

            responseBody.add(result);
            response.setMasterData(responseBody);

            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e){
            response.setMessage(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> updateMasterData(@RequestBody MDMSRequest request){
        MDMSResponse response = new MDMSResponse();
        try {
            validationService.validateMasterDataSchema(request.getMasterName(), request.getMasterData());
        }
        catch(Exception e) {
            String validationErrorMessage = "Invalid request body: " + e.getMessage();
            response.setMessage(validationErrorMessage);
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

        ArrayList<MDMSData> responseBody = new ArrayList<>();

        try {
            responseBody.add(service.updateMDMSData(request));
        }catch (Exception e){
            response.setMessage(e.getMessage());
        }
        response.setMessage("Master data updated");
        response.setMasterData(responseBody);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/_delete/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> deleteMasterData(@PathVariable int id){

        try{
            String result = "Master data deleted "+service.deleteMDMSData(id);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (Exception e) {
            String result = "Master data ID not present";
            return new ResponseEntity<>(result,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
