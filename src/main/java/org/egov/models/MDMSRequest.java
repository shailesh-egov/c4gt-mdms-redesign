package org.egov.models;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import javax.persistence.*;
import javax.validation.constraints.NotNull;



@Data
@AllArgsConstructor
@NoArgsConstructor
@TypeDef(name = "json", typeClass = JsonType.class)
public class MDMSRequest{

    private int id;

    @NotNull(message = "tenantId cannot be null")
    private String tenantId;

    @NotNull(message = "moduleName cannot be null")
    private String moduleName;

    @NotNull(message = "masterName cannot be null")
    private String masterName;

    @NotNull(message = "masterData cannot be null")
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private JsonNode masterData;
}
