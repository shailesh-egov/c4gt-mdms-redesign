package org.egov.service;

import org.egov.models.MDMSRequest;
import org.egov.repository.MDMSRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MDMSService {

    @Autowired
    private MDMSRepository repository;

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSRequest saveMDMSData(MDMSRequest request) {
        return repository.save(request);
    }

    @Cacheable(value = "mdmsDataCache")
    public List<MDMSRequest> getMDMSData() {
        return repository.findAll();
    }

    @Cacheable( value = "mdmsDataCache",key = "#id")
    public MDMSRequest getMDMSDataById(int id) {
        return repository.findById(id).orElse(null);
    }

    @CacheEvict(value = "mdmsDataCache", allEntries = true)
    public MDMSRequest updateMDMSData(MDMSRequest request) {
        MDMSRequest existingData = repository.findById(request.getId()).orElse(null);

        existingData.setTenantId(request.getTenantId());
        existingData.setModuleName(request.getModuleName());
        existingData.setMasterName(request.getMasterName());
        existingData.setMasterData(request.getMasterData());

        return repository.save(existingData);
    }

    @CacheEvict( value = "mdmsDataCache",key = "#id")
    public String deleteMDMSData(int id) {
        repository.deleteById(id);
        return "Request removed !! " + id;
    }
}
