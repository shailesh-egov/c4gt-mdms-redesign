package org.egov.models;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
@Entity
@Table(name="Master_Data")
@TypeDef(name = "json", typeClass = JsonType.class)
public class MDMSRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotNull
    private String tenantId;
    @NotNull
    private String moduleName;
    @NotNull
    private String masterName;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private JsonNode masterData;
}
