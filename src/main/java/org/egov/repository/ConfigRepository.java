package org.egov.repository;

import org.egov.models.MasterConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfigRepository extends JpaRepository<MasterConfig,Integer> {
    List<MasterConfig> findAllByModuleName(String moduleName);
    MasterConfig findByModuleNameAndMasterName(String moduleName,String masterName);

}
