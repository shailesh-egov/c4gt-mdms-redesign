# C4GT: Redesign and rewrite of MDMS.

### Persister configf file (mdms-persister.yml)

```
serviceMaps:
  serviceName: Mdms-Service
  mappings:
    - version: 1.0
      description: Persists the MDMS data
      fromTopic: save-mdms-data
      isTransaction: true
      queryMaps:
        - query: INSERT INTO master_data_table (id ,tenant_id, module_name,master_name,master_data) VALUES (?, ?, ?, ?, ?);
          basePath: $.MDMSData
          jsonMaps:
            - jsonPath: $.MDMSData.id

            - jsonPath: $.MDMSData.tenantId

            - jsonPath: $.MDMSData.moduleName

            - jsonPath: $.MDMSData.masterName

            - jsonPath: $.MDMSData.masterData
              type: JSON
              dbType: JSONB
    - version: 1.0
      description: Persists the MDMS data
      fromTopic: update-mdms-data
      isTransaction: true
      queryMaps:
        - query: UPDATE master_data_table SET master_data=? WHERE id=? AND tenant_id=? AND module_name=? AND master_name=?;
          basePath: $.MDMSData
          jsonMaps:
            - jsonPath: $.MDMSData.masterData

            - jsonPath: $.MDMSData.id

            - jsonPath: $.MDMSData.tenantId

            - jsonPath: $.MDMSData.moduleName

            - jsonPath: $.MDMSData.masterName
              type: JSON
              dbType: JSONB
```
