package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class MDMSSchemaRequest {
    @JsonProperty("RequestInfo")
    @Valid
    @NotNull(message = "RequestInfo cannot be null")
    private RequestInfo requestInfo;

    @JsonProperty("MDMSSchema")
    @Valid
    @NotNull(message = "MDMS Schema cannot be null")
    private MDMSSchema mdmsSchema;
}
