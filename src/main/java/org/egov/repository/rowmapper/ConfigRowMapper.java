package org.egov.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.models.MasterConfig;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigRowMapper implements RowMapper<MasterConfig> {

    @Override
    public MasterConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
        ObjectMapper objectMapper = new ObjectMapper();
        MasterConfig masterConfig = new MasterConfig();

        masterConfig.setId(rs.getString("id"));
        masterConfig.setModuleName(rs.getString("module_name"));
        masterConfig.setMasterName(rs.getString("master_name"));
        masterConfig.setIsStateLevel(rs.getBoolean("is_state_level"));

        PGobject pgObject = (PGobject) rs.getObject("unique_keys");
        try {
            // Convert the PGobject to a JsonNode using the ObjectMapper
            JsonNode jsonNode = objectMapper.readTree(pgObject.getValue());
            // Set the JsonNode in your schema object
            masterConfig.setUniqueKeys(jsonNode);

        } catch (Exception e) {
            // Handle exceptions
            throw new RuntimeException("Error occurred while mapping master config data columns");
        }

        return masterConfig;
    }
}
