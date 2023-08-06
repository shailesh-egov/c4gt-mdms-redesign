package org.egov.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MDMSResponse {
    @JsonProperty("message")
    private String message = null;

    @JsonProperty("master-data")
    private ArrayList<MDMSData> masterData = new ArrayList<>();

}
