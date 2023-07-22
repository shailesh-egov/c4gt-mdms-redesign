package org.egov.models;

import com.fasterxml.jackson.databind.JsonNode;

public class MDMSRequest {
    private String tenantId;
    private String moduleName;
    private String masterName;
    private JsonNode masterData;
}
