package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
public class MasterConfig {
    @JsonProperty("id")
    private String id = UUID.randomUUID().toString();

    @JsonProperty("moduleName")
    @NotNull(message = "moduleName cannot be null")
    private String moduleName;

    @JsonProperty("masterName")
    @NotNull(message = "masterName cannot be null")
    private String masterName;

    @JsonProperty("isStateLevel")
    private Boolean isStateLevel;

    @JsonProperty("uniqueKeys")
    private JsonNode uniqueKeys;

}
