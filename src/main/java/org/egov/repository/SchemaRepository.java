package org.egov.repository;

import org.egov.models.MDMSSchema;
import org.egov.repository.rowmapper.SchemaRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SchemaRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public MDMSSchema findByMasterName(String masterName) {
        String query = "SELECT * FROM master_data_schemas WHERE master_name = ?";

        MDMSSchema response = jdbcTemplate.queryForObject(query, new Object[] { masterName }, new SchemaRowMapper());
        return response;
    }

}
