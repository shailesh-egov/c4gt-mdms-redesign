package org.egov.repository;

import org.egov.models.MDMSRequest;
import org.springframework.data.jpa.repository.JpaRepository;

//JPA Repository is used as it has all the basic inbuilt functions implemented
public interface MDMSRepository extends JpaRepository<MDMSRequest,Integer> {
}
