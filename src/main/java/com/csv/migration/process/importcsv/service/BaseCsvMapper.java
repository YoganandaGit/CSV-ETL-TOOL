package com.csv.migration.process.importcsv.service;

import java.util.List;

public interface BaseCsvMapper {

    List<String> maptoSql(String tableName, String columns, List<String> rows, String whereCondition);

    String maptoLiquibase(String tableName, String columns, List<String> rows, String header, String closeTag, String systemId, String whereCondition);

}
