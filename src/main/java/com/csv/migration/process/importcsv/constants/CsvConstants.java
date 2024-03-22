package com.csv.migration.process.importcsv.constants;

public interface CsvConstants {
    String LIQUIBASE_ID_DATE_FORMAT = "yyyyMMddHHmmss";
    String CSV_XML_HEADER = """
            <?xml version="1.1" encoding="UTF-8" standalone="no"?>
             <databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">
            """;
    String CHANGE_SET_XML_HEADER = "    <changeSet id=\"%s\" author=\"%s\">" + System.lineSeparator();
    String DELETE_STATEMENT_XML = "       <delete tableName=\"%s\"><where></where></delete>" + System.lineSeparator();
    String DELETE_STATEMENT_WITH_WHERE_CLAUSE_XML = "       <delete tableName=\"%s\"><where>%s</where></delete>" + System.lineSeparator();
    String INSERT_STATEMENT_XML = "        <insert tableName=\"%s\">" + System.lineSeparator();
    String COLUMN_NAME_VALUE_XML_STRING = "            <column name=\"%s\" value=\"%s\"/>" + System.lineSeparator();
    String INSERT_XML_END_TAG = "        </insert>" + System.lineSeparator();
    String CHANGESET_XML_CLOSE_TAG = "    </changeSet>" + System.lineSeparator();
    String DELETE_STATEMENT_SQL = "DELETE FROM %s";
    String DELETE_STATEMENT_SQL_WHERE_CONDITION = " WHERE %s;";
    String INSERT_STATEMENT_SQL = "Insert into %s (%s) values (%s);";
}
