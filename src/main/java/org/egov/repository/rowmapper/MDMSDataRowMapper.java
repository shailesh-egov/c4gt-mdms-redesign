package org.egov.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.models.MDMSData;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MDMSDataRowMapper implements RowMapper<MDMSData> {

    @Override
    public MDMSData mapRow(ResultSet rs, int rowNum) throws SQLException {
        ObjectMapper objectMapper = new ObjectMapper();
        MDMSData mdmsObject = new MDMSData();

        mdmsObject.setId(rs.getString("id"));
        mdmsObject.setTenantId(rs.getString("tenant_id"));
        mdmsObject.setModuleName(rs.getString("module_name"));
        mdmsObject.setMasterName(rs.getString("master_name"));

        PGobject pgObject = (PGobject) rs.getObject("master_data");
        try {
            // Convert the PGobject to a JsonNode using the ObjectMapper
            JsonNode jsonNode = objectMapper.readTree(pgObject.getValue());
            // Set the JsonNode in your schema object
            mdmsObject.setMasterData(jsonNode);

        } catch (Exception e) {
            // Handle exceptions
            throw new RuntimeException("Error occurred while mapping master data columns");
        }
        return mdmsObject;
    }
}
