package org.egov.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.egov.config.MDMSConfig;
import org.egov.kafka.Producer;
import org.egov.models.MDMSSchemaRequest;
import org.egov.repository.SchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

@Service
public class JsonValidationService {

    @Autowired
    private SchemaRepository repository;

    @Autowired
    private Producer producer;

    @Autowired
    private MDMSConfig config;

    public void validateMasterDataSchema(String masterName, JsonNode masterData) {

        // Fetching Schema for masterName from DB
        JsonNode JsonSchema = repository.findByMasterName(masterName).getMasterDataSchema();

        // Converting JsonNode to InputStream
        String jsonString = JsonSchema.toString();
        InputStream schemaAsStream = new ByteArrayInputStream(jsonString.getBytes());

        // Loading the JSON schema
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(schemaAsStream);

        // Validation call
        Set<ValidationMessage> errors = schema.validate(masterData);

        StringBuilder errorsCombined = new StringBuilder();

        // Checking for errors
        for (ValidationMessage error : errors) {
            errorsCombined.append(error.toString());
            errorsCombined.append("\n");
        }

        // If errors are present else validation success
        if (errors.size() > 0)
            throw new RuntimeException("Please fix request body " + errorsCombined);
    }

    public MDMSSchemaRequest createMasterDataSchema(MDMSSchemaRequest request) {
        producer.push(config.getSaveMDMDSSchemaTopic(), request);
        return request;
    }

    public MDMSSchemaRequest updateMasterDataSchema(MDMSSchemaRequest request){
        producer.push(config.getUpdateMDMDSSchemaTopic(), request);
        return request;
    }



}
