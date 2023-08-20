package org.egov.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.egov.controller.MDMSController;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Set;

@Service
public class JSONValidationService {
    public void validateMasterDataSchema(String masterName, JsonNode masterData)  {

        //Defining schema path ,here resources/models/masterName.schema.json
        String directoryName = "schemas";
        String fileTye = ".schema.json";
        String JSONSchemaPath = directoryName+"/"+masterName+fileTye;

        //Loading the JSON schema
        InputStream schemaAsStream = MDMSController.class.getClassLoader().getResourceAsStream(JSONSchemaPath);
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(schemaAsStream);

        //Validation call
        Set<ValidationMessage> errors = schema.validate(masterData);

        StringBuilder errorsCombined = new StringBuilder();

        //Checking for errors
        for (ValidationMessage error : errors) {
            errorsCombined.append(error.toString());
            errorsCombined.append("\n");
        }

        //If errors are present else validation success
        if (errors.size() > 0)
            throw new RuntimeException("Please fix your json! " + errorsCombined);
    }

}
