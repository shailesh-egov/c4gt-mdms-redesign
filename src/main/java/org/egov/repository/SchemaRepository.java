package org.egov.repository;

import org.egov.models.MDMSSchema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchemaRepository extends JpaRepository<MDMSSchema,Integer> {
    MDMSSchema findByMasterName(String masterName);
}
