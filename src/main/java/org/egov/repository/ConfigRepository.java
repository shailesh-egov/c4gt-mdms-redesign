package org.egov.repository;

import org.egov.models.MasterConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<MasterConfig,Integer> {

    boolean existsByModuleName(String moduleName);
    MasterConfig findByModuleNameAndMasterName(String moduleName,String masterName);

}
