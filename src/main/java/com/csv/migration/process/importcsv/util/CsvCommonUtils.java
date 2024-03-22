package com.csv.migration.process.importcsv.util;

import com.csv.migration.process.importcsv.constants.CsvConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public class CsvCommonUtils {

    public static String encloseValueInSingleQuotes(String value) {
        return '\'' + value + '\'';
    }

    public static String getUUID(String systemId) {
        FastDateFormat outputFormat = FastDateFormat.getInstance(CsvConstants.LIQUIBASE_ID_DATE_FORMAT);
        return systemId + "-" + outputFormat.format(System.currentTimeMillis());
    }

    public static void appendCommonChangelogInfo(StringBuilder changesSetBuilder, String tableName, String systemId, String whereCondition) {
        String uuid = getUUID(systemId);
        changesSetBuilder.append(String.format(CsvConstants.CHANGE_SET_XML_HEADER, uuid, systemId));
        changesSetBuilder.append(buildXmlDeleteStatement(tableName, whereCondition));
    }

    public static void appendCommonChangelogCloseTags(StringBuilder changesSetBuilder) {
        changesSetBuilder.append(CsvConstants.CHANGESET_XML_CLOSE_TAG);
    }

    public static String buildDeleteSqlStatement(String tableName, String whereCondition) {
        String deleteStatementBuilder = String.format(CsvConstants.DELETE_STATEMENT_SQL, tableName);
        if (StringUtils.isNotBlank(whereCondition)) {
            deleteStatementBuilder += String.format(CsvConstants.DELETE_STATEMENT_SQL_WHERE_CONDITION, whereCondition);
        } else {
            deleteStatementBuilder += ";";
        }
        return deleteStatementBuilder;
    }

    public static String buildXmlDeleteStatement(String tableName, String whereCondition) {
        String deleteStatementBuilder;
        if (StringUtils.isNotBlank(whereCondition)) {
            deleteStatementBuilder = String.format(CsvConstants.DELETE_STATEMENT_WITH_WHERE_CLAUSE_XML, tableName, whereCondition);
        } else {
            deleteStatementBuilder = String.format(CsvConstants.DELETE_STATEMENT_XML, tableName);
        }
        return deleteStatementBuilder;
    }
}
