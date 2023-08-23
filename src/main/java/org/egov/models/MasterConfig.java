package org.egov.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.Valid;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
@Entity
public class MasterConfig {
    @Id
    private int id;
    private String moduleName;
    private String masterName;
    private boolean isStateLevel;
    private List<String> uniqueKeys;

}
