package org.egov.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.models.MDMSData;
import org.egov.repository.rowmapper.MDMSDataRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class MDMSRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public MDMSData findByTenantIdAndModuleNameAndMasterName(String tenantId, String moduleName, String masterName) {
        MDMSData response = new MDMSData();
        String query = "SELECT * FROM master_data WHERE tenant_id = ? AND module_name= ? AND master_name = ?";

        try {
            response = jdbcTemplate.queryForObject(query, new Object[]{tenantId, moduleName, masterName}, new MDMSDataRowMapper());
        } catch (Exception e) {
            log.info("Exception : " + e);
        }
        return response;
    }

    public boolean existsByTenantId(String tenantId) {
        String query = "SELECT CASE WHEN EXISTS (\n" + "    SELECT 1\n" + "    FROM master_data\n" + "    WHERE tenant_id = ?\n" + ") THEN TRUE ELSE FALSE END";

        boolean response = jdbcTemplate.queryForObject(query, new Object[]{tenantId}, Boolean.class);
        return response;
    }

    public boolean existsByTenantIdAndModuleName(String tenantId, String moduleName) {

        String query = "SELECT CASE WHEN EXISTS (\n" + "    SELECT 1\n" + "    FROM master_data\n" + "    WHERE tenant_id = ? AND module_name = ? \n" + ") THEN TRUE ELSE FALSE END";
        boolean response = jdbcTemplate.queryForObject(query, new Object[]{tenantId, moduleName}, Boolean.class);
        return response;
    }
}

