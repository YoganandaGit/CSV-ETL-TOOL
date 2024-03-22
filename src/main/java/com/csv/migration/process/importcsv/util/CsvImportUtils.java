package com.csv.migration.process.importcsv.util;

import com.csv.migration.process.importcsv.constants.CsvConstants;
import com.csv.migration.process.importcsv.service.BaseCsvMapper;
import com.csv.migration.process.exception.CsvImportServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CsvImportUtils implements InitializingBean {

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${tablename.column.mapping}")
    private String tableNameColumnMapping;

    @Value("${tablename.mapper.name}")
    private String tableNameMapperName;

    @Value("${csv.mapper.system.id}")
    private String systeId;

    private Map<String, String> tableColumnMap;

    private Map<String, String> tableMapperNameMap;

    private String liquibaseXmlHeader;

    private String liquibaseXmlFooter = "</databaseChangeLog>";

    public List<String> getSqlList(String tableName, String whereCondition) throws CsvImportServiceException {
        List<String> sqlList;
        try {
            if (StringUtils.isBlank(tableColumnMap.get(tableName)) || StringUtils.isBlank(tableMapperNameMap.get(tableName))) {
                throw new CsvImportServiceException("Table Name not found in the mapping for the table name: " + tableName);
            }
            String columns = tableColumnMap.get(tableName);
            String mapperName = tableMapperNameMap.get(tableName);
            String fileName = "classpath:csv/" + tableName + ".csv";
            BaseCsvMapper baseCsvMapper = SpringServiceManager.getBean(mapperName);
            List<String> rows = getCsvAsRows(fileName);
            sqlList = baseCsvMapper.maptoSql(tableName, columns, rows, whereCondition);
        } catch (Exception e) {
            throw new CsvImportServiceException(e);
        }
        return sqlList;
    }

    public String getLiquibaseScripts(String tableName, String whereCondition) throws CsvImportServiceException {
        String liquibaseXml;
        try {
            if (StringUtils.isBlank(tableColumnMap.get(tableName)) || StringUtils.isBlank(tableMapperNameMap.get(tableName))) {
                throw new CsvImportServiceException("Table Name not found in the mapping for the table name: " + tableName);
            }
            String columns = tableColumnMap.get(tableName);
            String mapperName = tableMapperNameMap.get(tableName);
            String fileName = "classpath:csv/" + tableName + ".csv";
            BaseCsvMapper baseCsvMapper = SpringServiceManager.getBean(mapperName);
            List<String> rows = getCsvAsRows(fileName);
            liquibaseXml = baseCsvMapper.maptoLiquibase(tableName, columns, rows, liquibaseXmlHeader, liquibaseXmlFooter, systeId, whereCondition);
        } catch (Exception e) {
            throw new CsvImportServiceException(e);
        }
        return liquibaseXml;
    }

    public List<String> getCsvAsRows(String filePath) throws CsvImportServiceException {
        try {
            Resource resource = resourceLoader.getResource(filePath);
            File file = resource.getFile();
            return FileUtils.readLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CsvImportServiceException(e);
        }
    }

    @Override public void afterPropertiesSet() throws Exception {
        tableColumnMap = Arrays.stream(StringUtils.split(tableNameColumnMapping, "|"))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        tableMapperNameMap = Arrays.stream(StringUtils.split(tableNameMapperName, "|"))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        StringBuilder sb = new StringBuilder();
        liquibaseXmlHeader = CsvConstants.CSV_XML_HEADER; /*sb.
				append("<?xml version=\"1.1\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
				.append("<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n")
				.append("    xmlns:ext=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\"\n")
				.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
				.append("    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog-ext\n")
				.append("    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd\n")
				.append("    http://www.liquibase.org/xml/ns/dbchangelog\n")
				.append("    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd\">\n\n")
				.toString();*/
    }
}
