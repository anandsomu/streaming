package com.listener.beans;

import java.io.Serializable;
import org.knowm.yank.annotations.Column;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class InfoSchemaColumn implements Serializable {

    private static final long serialVersionUID = -5906863428964176971L;
    @Column("TABLE_CATALOG")
    private String tableCatalog;

    @Column("TABLE_SCHEMA")
    private String tableSchema;

    @Column("TABLE_NAME")
    private String tableName;

    @Column("COLUMN_NAME")
    private String columnName;

    @Column("ORDINAL_POSITION")
    private int ordinalPosition;
}
