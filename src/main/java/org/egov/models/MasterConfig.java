package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
public class MasterConfig {
    @JsonProperty("id")
    private String id;

    @JsonProperty("moduleName")
    private String moduleName;

    @JsonProperty("masterName")
    private String masterName;

    @JsonProperty("isStateLevel")
    private Boolean isStateLevel;

    @JsonProperty("uniqueKeys")
    private JsonNode uniqueKeys;

}
