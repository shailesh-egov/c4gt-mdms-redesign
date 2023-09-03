package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
@Builder
public class MDMSData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    @NotNull(message = "tenantId cannot be null")
    private String tenantId;

    @JsonProperty("moduleName")
    @NotNull(message = "moduleName cannot be null")
    private String moduleName;

    @JsonProperty("masterName")
    @NotNull(message = "masterName cannot be null")
    private String masterName;

    @JsonProperty("masterData")
    @NotNull(message = "masterData cannot be null")
    private JsonNode masterData;

    @JsonProperty("createdAt")
    private Date createdAt;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("updatedAt")
    private Date updatedAt;

    @JsonProperty("updatedBy")
    private String updatedBy;
}
