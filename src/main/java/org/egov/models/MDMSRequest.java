package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MDMSRequest {
    @JsonProperty("RequestInfo")
    @Valid
    @NotNull(message = "requestInfo cannot be null")
    private RequestInfo requestInfo = null;

    @JsonProperty("MDMSData")
    @Valid
    @NotNull(message = "MDMS Data cannot be null")
    private MDMSData mdmsData = null;
}
