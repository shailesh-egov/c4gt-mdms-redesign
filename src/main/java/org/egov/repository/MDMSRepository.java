package org.egov.repository;

import org.egov.models.MDMSData;
import org.springframework.data.jpa.repository.JpaRepository;

//JPA Repository is used as it has all the basic inbuilt functions implemented
public interface MDMSRepository extends JpaRepository<MDMSData, Integer> {

    MDMSData findByTenantIdAndModuleNameAndMasterName(String tenantId, String moduleName, String masterName);

    boolean existsByTenantId(String tenantId);

    boolean existsByTenantIdAndModuleName(String tenantId, String moduleName);
}
