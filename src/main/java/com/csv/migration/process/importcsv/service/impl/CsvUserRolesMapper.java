package com.csv.migration.process.importcsv.service.impl;

import com.csv.migration.process.importcsv.constants.CsvConstants;
import com.google.common.collect.Lists;
import com.csv.migration.process.importcsv.service.BaseCsvMapper;
import com.csv.migration.process.importcsv.util.CsvCommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("csvUserRolesMapper")
public class CsvUserRolesMapper implements BaseCsvMapper {

    @Override
    public List<String> maptoSql(String tableName, String columns, List<String> values, String whereCondition) {
        //USER_ID,ROLE_ID
        List<String> sqlList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(values)) {
            sqlList.add(CsvCommonUtils.buildDeleteSqlStatement(tableName, whereCondition));
            for (String value : values) {
                String[] valueArr = StringUtils.split(StringUtils.trim(value), ",");
                String userId = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[0]);
                String roleId = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[1]);
                String valueStr = Stream.of(userId, roleId).collect(Collectors.joining(","));
                sqlList.add(String.format(CsvConstants.INSERT_STATEMENT_SQL, tableName, columns, valueStr));
            }
        }
        return sqlList;
    }

    @Override
    public String maptoLiquibase(String tableName, String columns, List<String> rows, String header, String closeTag, String systemId, String whereCondition) {

        StringBuilder liquibaseXmlBuilder = new StringBuilder();
        liquibaseXmlBuilder.append(header);

        if (CollectionUtils.isNotEmpty(rows)) {
            StringBuilder changesSetBuilder = new StringBuilder();
            CsvCommonUtils.appendCommonChangelogInfo(changesSetBuilder, tableName, systemId, whereCondition);
            for (String row : rows) {
                String[] valueArr = StringUtils.split(StringUtils.trim(row), ",");

                //Build the column mappings
                String columnsBuilder = getColumnsXmlString(valueArr);

                //Add the insert statement
                changesSetBuilder.append(String.format(CsvConstants.INSERT_STATEMENT_XML, tableName))
                        .append(columnsBuilder)
                        .append(CsvConstants.INSERT_XML_END_TAG);
            }
            CsvCommonUtils.appendCommonChangelogCloseTags(changesSetBuilder);
            liquibaseXmlBuilder.append(changesSetBuilder);
        }

        liquibaseXmlBuilder.append(closeTag);

        return liquibaseXmlBuilder.toString();
    }

    private String getColumnsXmlString(String[] valueArr) {
        String userId = valueArr[0];
        String roleId = valueArr[1];

        //Build the column mappings
        return String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "USER_ID", userId)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "ROLE_ID", roleId);

    }

}
