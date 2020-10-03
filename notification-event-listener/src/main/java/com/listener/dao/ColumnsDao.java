package com.listener.dao;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.PostConstruct;

import com.listener.beans.InfoSchemaColumn;
import org.apache.commons.lang3.StringUtils;
import org.knowm.yank.Yank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class ColumnsDao {

    public static final String PRODUCT_DISTRIBUTION = "product_distribution";
    @Value("${mysql.binlog.host:localhost}")
    private String host;

    @Value("${mysql.binlog.port:3306}")
    private int port;

    @Value("${mysql.binlog.user}")
    private String user;

    @Value("${mysql.binlog.password}")
    private String password;

    @Getter
    private Map<String, Map<String, Integer>> tableColumnOridinals = new HashMap<>();

    private Map<String, List<InfoSchemaColumn>> tableColumns = new HashMap<>();

    @PostConstruct
    private void init() {
        tableColumns.putAll(getColumnsMetaInfo("source", PRODUCT_DISTRIBUTION));
    }

    private Map<String, List<InfoSchemaColumn>> getColumnsMetaInfo(String schemaName,
                                                                   String tableName) {
        // DB Properties
        Properties dbProps = new Properties();
        dbProps.setProperty("jdbcUrl", StringUtils.join("jdbc:mysql://", host, ":", port,
                                                        "/information_schema?serverTimezone=UTC"));
        dbProps.setProperty("username", user);
        dbProps.setProperty("password", password);
        dbProps.setProperty("maximumPoolSize", "5");

        Yank.setupDefaultConnectionPool(dbProps);
        String sql = "SELECT TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,ORDINAL_POSITION - 1 AS "
                        + "ORDINAL_POSITION FROM information_schema.COLUMNS AS c "
                        + "WHERE c.TABLE_SCHEMA=? AND c.TABLE_NAME=? AND c.TABLE_CATALOG=\"def\"";

        Object[] params = new Object[] {schemaName, tableName};

        List<InfoSchemaColumn> columnsBean =
                        Yank.queryBeanList(sql, InfoSchemaColumn.class, params);

        return columnsBean.parallelStream().collect(groupingBy(InfoSchemaColumn::getTableName));
    }

    public Optional<String> columnNameForOrdinalPosition(String tableName, int position) {
        return tableColumns.getOrDefault(tableName, emptyList())
                           .parallelStream()
                           .filter(col -> col.getOrdinalPosition() == position)
                           .map(InfoSchemaColumn::getColumnName)
                           .findFirst();
    }
}
