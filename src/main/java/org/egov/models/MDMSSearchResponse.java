package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MDMSSearchResponse {

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("master-data-object")
    JsonNode masterData = null;
}
