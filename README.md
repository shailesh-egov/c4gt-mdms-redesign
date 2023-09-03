# C4GT: Redesign and rewrite of MDMS.

### Persister config file (mdms-persister.yml)

```
serviceMaps:
  serviceName: Mdms-Service
  mappings:
    - version: 1.0
      description: Persists the Master data
      fromTopic: save-mdms-data
      isTransaction: true
      queryMaps:
        - query: INSERT INTO master_data (id,tenant_id, module_name,master_name,master_data) VALUES ( ?,?, ?, ?, ?);
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
      description: Updates the Master data
      fromTopic: update-mdms-data
      isTransaction: true
      queryMaps:
        - query: UPDATE master_data SET master_data=? WHERE id=? AND tenant_id=? AND module_name=? AND master_name=?;
          basePath: $.MDMSData
          jsonMaps:
            - jsonPath: $.MDMSData.masterData
              type: JSON
              dbType: JSONB

            - jsonPath: $.MDMSData.id

            - jsonPath: $.MDMSData.tenantId

            - jsonPath: $.MDMSData.moduleName

            - jsonPath: $.MDMSData.masterName
    - version: 1.0
      description: Persists the Master Schemas used for master data validation
      fromTopic: save-mdms-schemas
      isTransaction: true
      queryMaps:
        - query: INSERT INTO master_data_schemas(master_name,master_data_schema) VALUES(?,?);
          basePath: $.MDMSSchema
          jsonMaps:
            - jsonPath: $.MDMSSchema.masterName

            - jsonPath: $.MDMSSchema.masterDataSchema
              type: JSON
              dbType: JSONB
    - version: 1.0
      description: Updates the Master Schemas used for master data validation
      fromTopic: update-mdms-schemas
      isTransaction: true
      queryMaps:
        - query: UPDATE master_data_schemas SET master_data_schema=? WHERE master_name=?;
          basePath: $.MDMSSchema
          jsonMaps:
            - jsonPath: $.MDMSSchema.masterDataSchema
              type: JSON
              dbType: JSONB

            - jsonPath: $.MDMSSchema.masterName
    - version: 1.0
      description: Persists the Master config used for fallback functionality
      fromTopic: save-mdms-config
      isTransaction: true
      queryMaps:
        - query: INSERT INTO master_config (id, module_name,master_name,is_state_level,unique_keys) VALUES ( ?,?, ?, ?, ?);
          basePath: $.MDMSConfig
          jsonMaps:
            - jsonPath: $.MDMSConfig.id

            - jsonPath: $.MDMSConfig.moduleName

            - jsonPath: $.MDMSConfig.masterName

            - jsonPath: $.MDMSConfig.isStateLevel

            - jsonPath: $.MDMSConfig.uniqueKeys
              type: JSON
              dbType: JSONB
    - version: 1.0
      description: Updates the Master config used for fallback functionality
      fromTopic: update-mdms-config
      isTransaction: true
      queryMaps:
        - query: UPDATE master_config SET is_state_level=?,unique_keys = ? WHERE id=? AND module_name=? AND master_name=?;
          basePath: $.MDMSConfig
          jsonMaps:
            - jsonPath: $.MDMSConfig.isStateLevel

            - jsonPath: $.MDMSConfig.uniqueKeys
              type: JSON
              dbType: JSONB

            - jsonPath: $.MDMSConfig.id

            - jsonPath: $.MDMSConfig.moduleName

            - jsonPath: $.MDMSConfig.masterName
```
