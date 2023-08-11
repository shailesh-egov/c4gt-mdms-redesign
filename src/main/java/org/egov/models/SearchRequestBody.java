package org.egov.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
public class SearchRequestBody {

    @NotNull(message = "masterName cannot be null")
    private String masterName;

    @NotNull(message = "searchKey cannot be null")
    private String searchKey;

    @NotNull(message = "searchValue cannot be null")
    private String searchValue;
}
