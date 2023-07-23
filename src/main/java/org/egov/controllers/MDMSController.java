package org.egov.controllers;


import org.egov.models.MDMSRequest;
import org.egov.service.MDMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping("/mdms/v1")
public class MDMSController {
    @Autowired
    private MDMSService service;
    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    public ResponseEntity<MDMSRequest> createMasterData(@RequestBody MDMSRequest request){

        return new ResponseEntity<>(service.saveMDMSData(request), HttpStatus.OK);
    }

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<List<MDMSRequest>> getMasterData(@RequestBody MDMSRequest request){
        return new ResponseEntity<>(service.getMDMSData(), HttpStatus.OK);
    }

    @RequestMapping(value = "/_search/{id}", method = RequestMethod.POST)
    public ResponseEntity<MDMSRequest> getMasterDataById(@PathVariable int id){
        return new ResponseEntity<>(service.getMDMSDataById(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    public ResponseEntity<MDMSRequest> updateMasterData(@RequestBody MDMSRequest request){
        return new ResponseEntity<>(service.updateMDMSData(request), HttpStatus.OK);
    }

    @RequestMapping(value = "/_delete/{id}", method = RequestMethod.POST)
    public ResponseEntity<String> deleteMasterData(@PathVariable int id){
        return new ResponseEntity<>(service.deleteMDMSData(id), HttpStatus.OK);
    }
}
