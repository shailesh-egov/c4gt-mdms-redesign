package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class MasterConfigRequest {
    @JsonProperty("RequestInfo")
    @Valid
    @NotNull(message = "RequestInfo cannot be null")
    private RequestInfo requestInfo;

    @JsonProperty("MDMSConfig")
    @Valid
    @NotNull(message = "MDMS Config cannot be null")
    private MasterConfig masterConfig;
}
