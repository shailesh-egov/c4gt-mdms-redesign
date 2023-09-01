package org.egov.models;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.Valid;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
@Entity
@Table(name="master_config_table")
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
public class MasterConfig {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "moduleName")
    private String moduleName;

    @Column(name = "masterName")
    private String masterName;

    @Column(name = "isStateLevel")
    private Boolean isStateLevel;

    @Type(type = "list-array")
    @Column(
            name = "uniqueKeys",
            columnDefinition = "text[]"
    )
    private List<String> uniqueKeys;

}
