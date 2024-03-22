package com.csv.migration.process.importcsv.service.impl;

import com.csv.migration.process.importcsv.constants.CsvConstants;
import com.google.common.collect.Lists;
import com.csv.migration.process.importcsv.service.BaseCsvMapper;
import com.csv.migration.process.importcsv.util.CsvCommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("csvUserInfoMapper")
public class CsvUserInfoMapper implements BaseCsvMapper {

    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");

    @Value("${csv.mapper.system.id}")
    private String systemId;

    @Override
    public List<String> maptoSql(String tableName, String columns, List<String> values, String whereCondition) {
        //USER_ID,USER_NAME,AUTHENTICATION_TYPE,STATUS,SYSTEM_ID,USER_ACCT_ID,CREATE_BY,CHANGE_PWD_FLAG,START_DATE,END_DATE,SYSTEM_ACCT_FLAG,ORGANIZATION
        List<String> sqlList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(values)) {
            sqlList.add(CsvCommonUtils.buildDeleteSqlStatement(tableName, whereCondition));
            for (String value : values) {
                String[] valueArr = StringUtils.split(StringUtils.trim(value), ",");
                String organization = "";
                String userId = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[0]);
                String userName = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[1]);
                String authenticationType = CsvCommonUtils.encloseValueInSingleQuotes("D");
                String status = CsvCommonUtils.encloseValueInSingleQuotes("A");
                String userAccountId = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[2]);
                String createBy = CsvCommonUtils.encloseValueInSingleQuotes("SYSTEM");
                String changePwdFlag = "1";
                String startDate = CsvCommonUtils.encloseValueInSingleQuotes(formatDate(valueArr[3]));
                String endDate = CsvCommonUtils.encloseValueInSingleQuotes(formatDate(valueArr[4]));
                String systemAcctFlag = CsvCommonUtils.encloseValueInSingleQuotes((StringUtils.equalsAnyIgnoreCase(valueArr[5], "N") ? "0" : "1"));
                if (ArrayUtils.getLength(valueArr) > 6 && StringUtils.isNotBlank(valueArr[6])) {
                    organization = CsvCommonUtils.encloseValueInSingleQuotes(valueArr[6]);
                } else {
                    organization = CsvCommonUtils.encloseValueInSingleQuotes("");
                }

                //Replace with Stream API
                String valueStr = Stream.of(userId, userName, authenticationType, status, CsvCommonUtils.encloseValueInSingleQuotes(systemId), userAccountId, createBy, changePwdFlag, startDate, endDate, systemAcctFlag, organization).collect(Collectors.joining(","));
                sqlList.add(String.format(CsvConstants.INSERT_STATEMENT_SQL, tableName, columns, valueStr));
            }
        }
        return sqlList;
    }

    private String formatDate(String inputDateString) {
        String outputDateString = null;
        try {
            Date date = inputFormat.parse(inputDateString);
            FastDateFormat outputFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
            outputDateString = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputDateString;
    }

    @Override
    public String maptoLiquibase(String tableName, String columns, List<String> rows, String header, String closeTag, String systemId, String whereCondition) {
        //USER_ID,USER_NAME,AUTHENTICATION_TYPE,STATUS,SYSTEM_ID,USER_ACCT_ID,CREATE_BY,CHANGE_PWD_FLAG,START_DATE,END_DATE,SYSTEM_ACCT_FLAG,ORGANIZATION
        StringBuilder liquibaseXmlBuilder = new StringBuilder();
        liquibaseXmlBuilder.append(header);

        if (CollectionUtils.isNotEmpty(rows)) {
            StringBuilder changesSetBuilder = new StringBuilder();
            CsvCommonUtils.appendCommonChangelogInfo(changesSetBuilder, tableName, systemId, whereCondition);
            for (String value : rows) {
                String[] valueArr = org.thymeleaf.util.StringUtils.split(org.thymeleaf.util.StringUtils.trim(value), ",");
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
        //USER_ID,USER_NAME,AUTHENTICATION_TYPE,STATUS,SYSTEM_ID,USER_ACCT_ID,CREATE_BY,CHANGE_PWD_FLAG,START_DATE,END_DATE,SYSTEM_ACCT_FLAG,ORGANIZATION
        String organization = "";
        String userId = valueArr[0];
        String userName = valueArr[1];
        String authenticationType = "D";
        String status = "A";
        String userAccountId = valueArr[2];
        String createBy = "SYSTEM";
        String changePwdFlag = "1";
        String startDate = formatDate(valueArr[3]);
        String endDate = formatDate(valueArr[4]);
        String systemAcctFlag = (StringUtils.equalsAnyIgnoreCase(valueArr[5], "N") ? "0" : "1");
        if (ArrayUtils.getLength(valueArr) > 6 && StringUtils.isNotBlank(valueArr[6])) {
            organization = valueArr[6];
        } else {
            organization = "";
        }

        //Build the column mappings
        return String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "USER_ID", userId)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "USER_NAME", userName)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "AUTHENTICATION_TYPE", authenticationType)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "STATUS", status)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "SYSTEM_ID", systemId)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "USER_ACCT_ID", userAccountId)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "CREATE_BY", createBy)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "CHANGE_PWD_FLAG", changePwdFlag)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "START_DATE", startDate)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "END_DATE", endDate)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "SYSTEM_ACCT_FLAG", systemAcctFlag)
                + String.format(CsvConstants.COLUMN_NAME_VALUE_XML_STRING, "ORGANIZATION", organization);
    }

}
