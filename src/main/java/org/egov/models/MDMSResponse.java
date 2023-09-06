package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MDMSResponse {
    @JsonProperty("ResponseInfo")
    @Valid
    private ResponseInfo responseInfo;

    @JsonProperty("master_data")
    @Valid
    private List<MDMSData> masterData;

    public MDMSResponse addMDMSItem(MDMSData mdmsItem) {
        if (this.masterData == null) {
            this.masterData = new ArrayList<>();
        }
        this.masterData.add(mdmsItem);
        return this;
    }

}
