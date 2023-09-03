package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid


public class MDMSSchema {

    @JsonProperty("masterName")
    @NotNull(message = "MasterName cannot be null")
    private String  masterName;

    @NotNull(message = "MasterData Schema cannot be null")
    @JsonProperty("masterDataSchema")
    private JsonNode masterDataSchema;
}
