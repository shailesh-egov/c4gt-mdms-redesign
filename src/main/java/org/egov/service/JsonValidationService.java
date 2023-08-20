package org.egov.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.egov.controller.MDMSController;
import org.egov.models.MDMSSchema;
import org.egov.repository.MDMSRepository;
import org.egov.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

@Service
@Slf4j
public class JsonValidationService {

    @Autowired
    private SchemaRepository repository;
    public void validateMasterDataSchema(String masterName, JsonNode masterData)  {

        JsonNode JsonSchema = repository.findByMasterName(masterName).getMasterDataSchema();

        // Converting JsonNode to InputStream
        String jsonString = JsonSchema.toString();
        InputStream schemaAsStream = new ByteArrayInputStream(jsonString.getBytes());

        //Loading the JSON schema
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

    public MDMSSchema addMasterDataSchema(MDMSSchema request){
        return repository.save(request);
    }

}
