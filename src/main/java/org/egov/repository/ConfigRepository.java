package org.egov.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.models.MasterConfig;
import org.egov.repository.rowmapper.ConfigRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class ConfigRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean existsByModuleName(String moduleName) {

        String query = "SELECT CASE WHEN EXISTS (\n" + "    SELECT 1\n" + "    FROM master_config\n" + "    WHERE module_name = ?\n" + ") THEN TRUE ELSE FALSE END";

        boolean response = jdbcTemplate.queryForObject(query, new Object[]{moduleName}, Boolean.class);
        return response;
    }

    public MasterConfig findByModuleNameAndMasterName(String moduleName, String masterName) {
        String query = "SELECT * FROM master_config WHERE module_name= ? AND master_name = ?";
        MasterConfig response = new MasterConfig();
        try {
            response = jdbcTemplate.queryForObject(query, new Object[]{moduleName, masterName}, new ConfigRowMapper());
        } catch (Exception e) {
            log.info("Exception : " + e);
        }

        return response;
    }

}
