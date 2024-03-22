package com.csv.migration.process.importcsv.service.impl;

import com.google.common.collect.Lists;
import com.csv.migration.process.importcsv.constants.CsvConstants;
import com.csv.migration.process.importcsv.service.BaseCsvMapper;
import com.csv.migration.process.importcsv.util.CsvCommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("csvUserCredentialsMapper")
public class CsvUserCredentialsMapper implements BaseCsvMapper {

    @Override
    public List<String> maptoSql(String tableName, String columns, List<String> values, String whereCondition) {
        //USER_ID,CREDENTIAL_TYPE,USER_ACCT_ID,PASSWORD,PASSWORD_STORE,PASSWORD_TYPE,RETRIES,DATETIME_EXPIRY
        List<String> sqlList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(values)) {
            sqlList.add(CsvCommonUtils.buildDeleteSqlStatement(tableName, whereCondition));
            for (String value : values) {
                String[] valueArr = StringUtils.split(StringUtils.trim(value), ",");
                String userId = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[0]);
                String credentialType = CsvCommonUtils.encloseValueInSingleQuotes("CSVSYSTEM");
                String userAccountId = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[1]);
                String password = CsvCommonUtils.encloseValueInSingleQuotes("fbeff221e0b37ca9042ea5fcb13c5ef59d85ddf4");
                String passwordStore = CsvCommonUtils.encloseValueInSingleQuotes("DEFAULT");
                String passwordType = CsvCommonUtils.encloseValueInSingleQuotes("password");
                String retries = "0";
                String dateTimeExpiry = CsvCommonUtils.encloseValueInSingleQuotes(getExpiryDate());
                //Replace with Stream API
                String valueStr = Stream.of(userId, credentialType, userAccountId, password, passwordStore, passwordType, retries, dateTimeExpiry).collect(Collectors.joining(","));
                sqlList.add(String.format(CsvConstants.INSERT_STATEMENT_SQL, tableName, columns, valueStr));
            }
        }
        return sqlList;
    }

    private String getExpiryDate() {
        // Define the number of days to add
        int daysToAdd = 90;

        // Get the current date
        Date currentDate = new Date();

        // Use DateUtils to add days to the current date
        Date futureDate = DateUtils.addDays(currentDate, daysToAdd);

        FastDateFormat outputFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        return outputFormat.format(futureDate);
    }

    @Override
    public String maptoLiquibase(String tableName, String columns, List<String> rows, String header, String closeTag, String systemId, String whereCondition) {
        //USER_ID,CREDENTIAL_TYPE,USER_ACCT_ID,PASSWORD,PASSWORD_STORE,PASSWORD_TYPE,RETRIES,DATETIME_EXPIRY
        StringBuilder liquibaseXmlBuilder = new StringBuilder();
        liquibaseXmlBuilder.append(header);

        if (CollectionUtils.isNotEmpty(rows)) {
            StringBuilder changesSetBuilder = new StringBuilder();
            CsvCommonUtils.appendCommonChangelogInfo(changesSetBuilder, tableName, systemId, whereCondition);
            for (String value : rows) {
                String[] valueArr = StringUtils.split(StringUtils.trim(value), ",");
                String columnsBuilder = getColumnXmlString(valueArr);

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

    private String getColumnXmlString(String[] valueArr) {
        String userId = valueArr[0];
        String credentialType = "CSVSYSTEM";
        String userAccountId = valueArr[1];
        String password = "fbeff221e0b37ca9042ea5fcb13c5ef59d85ddf4";
        String passwordStore = "DEFAULT";
        String passwordType = "password";
        String retries = "0";
        String dateTimeExpiry = getExpiryDate();

        //Build the column mappings
        return String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "USER_ID", userId)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "CREDENTIAL_TYPE", credentialType)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "USER_ACCT_ID", userAccountId)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "PASSWORD", password)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "PASSWORD_STORE", passwordStore)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "PASSWORD_TYPE", passwordType)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "RETRIES", retries)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "DATETIME_EXPIRY", dateTimeExpiry);
    }
}
