package org.egov.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.models.MDMSSchema;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


@Slf4j
public class SchemaRowMapper implements RowMapper<MDMSSchema> {

    @Override
    public MDMSSchema mapRow(ResultSet rs, int rowNum) throws SQLException {
        ObjectMapper objectMapper = new ObjectMapper();
        MDMSSchema schema = new MDMSSchema();

        schema.setMasterName(rs.getString("master_name"));

        PGobject pgObject = (PGobject) rs.getObject("master_data_schema");
        try {
            // Convert the PGobject to a JsonNode using the ObjectMapper
            JsonNode jsonNode = objectMapper.readTree(pgObject.getValue());
            // Set the JsonNode in your schema object
            schema.setMasterDataSchema(jsonNode);

        } catch (Exception e) {
            // Handle exceptions
            throw new RuntimeException("Error occurred while mapping master data schema columns");
        }
        return schema;
    }
}
