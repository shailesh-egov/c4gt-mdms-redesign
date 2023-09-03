package org.egov.controller;

import digit.models.coremodels.mdms.MdmsCriteriaReq;
import digit.models.coremodels.mdms.MdmsResponse;
import net.minidev.json.JSONArray;
import org.egov.models.*;
import org.egov.service.JsonValidationService;
import org.egov.service.MDMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/mdms/v1")
public class MDMSController {
    @Autowired
    private MDMSService mdmsService;

    @Autowired
    private JsonValidationService validationService;

    private MDMSResponse buildResponse(String message, ArrayList<MDMSData> masterDataBody) {
        MDMSResponse response = new MDMSResponse();
        response.setMessage(message);
        response.setMasterData(masterDataBody);

        return response;
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> createMasterData(@RequestBody MDMSRequest request) {
        ArrayList<MDMSData> responseBody = new ArrayList<>();
        String message;

        try {
            validationService.validateMasterDataSchema(request.getMdmsData().getMasterName(),
                    request.getMdmsData().getMasterData());
        } catch (Exception e) {
            message = "Invalid request body: " + e.getMessage();
            MDMSResponse response = buildResponse(message, responseBody);

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            mdmsService.saveMDMSData(request);
        } catch (Exception e) {
            message = "Invalid request body: " + e.getMessage();
            MDMSResponse response = buildResponse(message, responseBody);

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        message = "Creation successful";
        MDMSResponse response = buildResponse(message, responseBody);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    private ResponseEntity<?> search(@RequestBody @Valid MdmsCriteriaReq mdmsCriteriaReq) {
        Map<String, Map<String, JSONArray>> response;
        try {
            response = mdmsService.searchMaster(mdmsCriteriaReq);
        } catch (Exception e) {
            throw new RuntimeException("Could not find master data :" + e.getMessage());
        }
        MdmsResponse mdmsResponse = new MdmsResponse();
        mdmsResponse.setMdmsRes(response);

        return new ResponseEntity<>(mdmsResponse, HttpStatus.OK);
    }


    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> updateMasterData(@RequestBody MDMSRequest request) {
        ArrayList<MDMSData> responseBody = new ArrayList<>();
        String message;
        try {
            validationService.validateMasterDataSchema(request.getMdmsData().getMasterName(),
                    request.getMdmsData().getMasterData());
        } catch (Exception e) {
            message = "Invalid request body: " + e.getMessage();
            MDMSResponse response = buildResponse(message, responseBody);

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            mdmsService.updateMDMSData(request);
        } catch (Exception e) {
            message = e.getMessage();
            MDMSResponse response = buildResponse(message, responseBody);

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        message = "Master data updated";
        MDMSResponse response = buildResponse(message, responseBody);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/_delete/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> deleteMasterData(@PathVariable int id) {

        try {
            String message = "Master data deleted " + mdmsService.deleteMDMSData(id);
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (Exception e) {
            String message = "Master data ID not present";
            return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value = "/_create/schema", method = RequestMethod.POST)
    public ResponseEntity<MDMSSchemaRequest> deleteMasterData(@RequestBody MDMSSchemaRequest request) {
        return new ResponseEntity<>(validationService.addMasterDataSchema(request), HttpStatus.OK);
    }

    @RequestMapping(value = "/_create/config", method = RequestMethod.POST)
    public ResponseEntity<MasterConfigRequest> createMasterDataConfig(@RequestBody MasterConfigRequest request) {
        return new ResponseEntity<>(mdmsService.createMasterConfigData(request), HttpStatus.OK);
    }

}
