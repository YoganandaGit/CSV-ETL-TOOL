package com.csv.migration.process.importcsv.service.impl;

import com.google.common.collect.Lists;
import com.csv.migration.process.importcsv.constants.CsvConstants;
import com.csv.migration.process.importcsv.service.BaseCsvMapper;
import com.csv.migration.process.importcsv.util.CsvCommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("csvOrganizationInfoMapper")
public class CsvOrganizationInfoMapper implements BaseCsvMapper {

    @Value("${csv.mapper.system.id}")
    private String systemId;

    @Override
    public List<String> maptoSql(String tableName, String columns, List<String> values, String whereCondition) {
        List<String> sqlList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(values)) {
            sqlList.add(CsvCommonUtils.buildDeleteSqlStatement(tableName, whereCondition));
            for (String value : values) {
                String[] valueArr = StringUtils.split(StringUtils.trim(value), ",");
                String valueStr = getString(valueArr);
                sqlList.add(String.format(CsvConstants.INSERT_STATEMENT_SQL, tableName, columns, valueStr));
            }
        }
        return sqlList;
    }

    private String getString(String[] valueArr) {
        //ORGANIZATION_TYPE,ORGANIZATION_NAME,ORGANIZATION_ID,SYSTEM_ID,STATUS,DATETIME_CREATE,CREATE_BY
        String orgType = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[0]);
        String orgName = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[1]);
        String orgId = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[2]);
        String status = CsvCommonUtils.encloseValueInSingleQuotes("active");
        return Stream.of(orgType, orgName, orgId, CsvCommonUtils.encloseValueInSingleQuotes(systemId), status, "getDate()", CsvCommonUtils.encloseValueInSingleQuotes("SYSTEM")).collect(Collectors.joining(","));
    }

    @Override
    public String maptoLiquibase(String tableName, String columns, List<String> rows, String header, String closeTag, String systemId, String whereCondition) {
        //ORGANIZATION_TYPE,ORGANIZATION_NAME,ORGANIZATION_ID,SYSTEM_ID,STATUS,DATETIME_CREATE,CREATE_BY
        StringBuilder liquibaseXmlBuilder = new StringBuilder();
        liquibaseXmlBuilder.append(header);

        if (CollectionUtils.isNotEmpty(rows)) {
            StringBuilder changesSetBuilder = new StringBuilder();
            CsvCommonUtils.appendCommonChangelogInfo(changesSetBuilder, tableName, systemId, whereCondition);
            for (String row : rows) {
                String[] valueArr = StringUtils.split(StringUtils.trim(row), ",");
                //Build the column mappings
                String columnsBuilder = getColumnXmlString(valueArr, systemId);
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

    private String getColumnXmlString(String[] valueArr, String systemId) {
        String orgType = valueArr[0];
        String orgName = valueArr[1];
        String orgId = valueArr[2];
        String status = "active";

        //Build the column mappings
        return String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "ORGANIZATION_TYPE", orgType)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "ORGANIZATION_ID", orgId)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "ORGANIZATION_NAME", orgName)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "SYSTEM_ID", systemId)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "STATUS", status)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "DATETIME_CREATE", "${now}")
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "CREATE_BY", "SYSTEM");

    }
}
