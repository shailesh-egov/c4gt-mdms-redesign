package org.egov.repository;

import org.egov.models.MDMSRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MDMSRepository extends JpaRepository<MDMSRequest,Integer> {
}
