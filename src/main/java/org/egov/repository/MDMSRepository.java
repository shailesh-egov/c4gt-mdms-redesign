package org.egov.repository;

import org.egov.models.MDMSData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//JPA Repository is used as it has all the basic inbuilt functions implemented
public interface MDMSRepository extends JpaRepository<MDMSData,Integer> {

    MDMSData findByTenantIdAndModuleNameAndMasterName(String tenantId, String moduleName, String masterName);
    List<MDMSData> findAllByTenantId(String tenantId);

    List<MDMSData> findAllByTenantIdAndModuleName(String tenantId,String moduleName);

}
