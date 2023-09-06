package org.egov.controller;

import digit.models.coremodels.mdms.MdmsCriteriaReq;
import digit.models.coremodels.mdms.MdmsResponse;
import net.minidev.json.JSONArray;
import org.egov.models.MDMSRequest;
import org.egov.models.MDMSResponse;
import org.egov.models.MDMSSchemaRequest;
import org.egov.models.MasterConfigRequest;
import org.egov.service.JsonValidationService;
import org.egov.service.MDMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/mdms/v1")
public class MDMSController {
    @Autowired
    private MDMSService mdmsService;

    @Autowired
    private JsonValidationService validationService;


    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<MDMSResponse> createMDMSData(@RequestBody MDMSRequest request) {

        try {
            validationService.validateMasterDataSchema(request.getMdmsData().getMasterName(), request.getMdmsData().getMasterData());
        } catch (Exception e) {
            throw new RuntimeException("Validation Error: ", e);
        }

        MDMSResponse mdmsResponse = mdmsService.createMDMSData(request);
        return new ResponseEntity<>(mdmsResponse, HttpStatus.CREATED);
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
    public ResponseEntity<MDMSResponse> updateMDMSData(@RequestBody MDMSRequest request) {

        try {
            validationService.validateMasterDataSchema(request.getMdmsData().getMasterName(), request.getMdmsData().getMasterData());
        } catch (Exception e) {
            throw new RuntimeException("Validation Error: ", e);
        }
        MDMSResponse mdmsResponse = mdmsService.updateMDMSData(request);
        return new ResponseEntity<>(mdmsResponse, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/_create/schema", method = RequestMethod.POST)
    public ResponseEntity<MDMSSchemaRequest> createMasterDataSchema(@RequestBody MDMSSchemaRequest request) {
        return new ResponseEntity<>(validationService.createMasterDataSchema(request), HttpStatus.OK);
    }

    @RequestMapping(value = "/_update/schema", method = RequestMethod.POST)
    public ResponseEntity<MDMSSchemaRequest> updatedMasterDataSchema(@RequestBody MDMSSchemaRequest request) {
        return new ResponseEntity<>(validationService.updateMasterDataSchema(request), HttpStatus.OK);
    }

    @RequestMapping(value = "/_create/config", method = RequestMethod.POST)
    public ResponseEntity<MasterConfigRequest> createMasterDataConfig(@RequestBody MasterConfigRequest request) {
        return new ResponseEntity<>(mdmsService.createMasterConfigData(request), HttpStatus.OK);
    }

    @RequestMapping(value = "/_update/config", method = RequestMethod.POST)
    public ResponseEntity<MasterConfigRequest> updateMasterDataConfig(@RequestBody MasterConfigRequest request) {
        return new ResponseEntity<>(mdmsService.updateMasterConfigData(request), HttpStatus.OK);
    }

}
