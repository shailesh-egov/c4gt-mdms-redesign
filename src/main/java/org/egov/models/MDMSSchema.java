package org.egov.models;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
@Entity
@Table(name="master_data_schemas")
@TypeDef(name = "json", typeClass = JsonType.class)

public class MDMSSchema implements Serializable {

    @Id
    private String  masterName;

    @NotNull(message = "masterData cannot be null")
    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private JsonNode masterDataSchema;
}
