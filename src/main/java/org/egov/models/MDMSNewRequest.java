package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MDMSNewRequest {
    @JsonProperty("RequestInfo")
    @Valid
    private RequestInfo requestInfo = null;

    @JsonProperty("MDMSData")
    @Valid
    private MDMSData mdmsData = null;
}
