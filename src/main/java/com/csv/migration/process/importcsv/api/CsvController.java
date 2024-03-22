package com.csv.migration.process.importcsv.api;

import com.csv.migration.process.exception.CsvImportServiceException;
import com.csv.migration.process.importcsv.util.CsvImportUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/csv")
@Tag(name = "CSV Imports", description = "CSV Import APIs")
public class CsvController {
    private final CsvImportUtils csvImportUtils;

    public CsvController(CsvImportUtils csvImportUtils) {
        this.csvImportUtils = csvImportUtils;
    }

    @GetMapping("/import")
    @Operation(summary = "Get Insrt SQLs for the given table name In the RAW format",
            description = "Get Insrt SQLs for the given table name in RAW format",
            tags = { "CSV Imports" })
    @ApiResponse(responseCode = "200", description = "Found the table",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = List.class)) })
    @ApiResponse(responseCode = "204", description = "Table data not found")
    public ResponseEntity<?> search(@RequestParam(value = "tableName") String tableName, @RequestParam(value = "whereCondition", required = false) String whereCondition) {
        List<String> sqlList;
        try {
            sqlList = csvImportUtils.getSqlList(tableName, whereCondition);
            if (CollectionUtils.isNotEmpty(sqlList)) {
                return new ResponseEntity<>(sqlList, HttpStatus.OK);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (CsvImportServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/downloadcsv")
    @Operation(summary = "Get Insrt SQLs for the given table name as a downloadable file",
            description = "Get Insrt SQLs for the given table name as a downloadable file",
            tags = { "CSV Imports Download" })
    @ApiResponse(responseCode = "200", description = "Found the table",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ByteArrayResource.class)) })
    @ApiResponse(responseCode = "204", description = "Table data not found")
    public ResponseEntity<?> downloadFile(@RequestParam(value = "tableName") String tableName, @RequestParam(value = "whereCondition", required = false) String whereCondition) {
        try {
            List<String> sqlList = csvImportUtils.getSqlList(tableName, whereCondition);

            if (CollectionUtils.isNotEmpty(sqlList)) {

                // Example: Generate or fetch the file content
                byte[] fileContent = StringUtils.join(sqlList, System.lineSeparator()).getBytes();

                // Create a ByteArrayResource from the byte array
                ByteArrayResource resource = new ByteArrayResource(fileContent);

                // Set the filename or other properties as needed
                String filename = tableName + ".sql";

                // Build the response entity
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM) // or use the appropriate media type
                        .contentLength(fileContent.length)
                        .body(resource);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (CsvImportServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/downloadliquibase")
    @Operation(summary = "Get Insrt SQLs for the given table name as a downloadable liquibase xml file",
            description = "Get Insrt SQLs for the given table name as a  liquibase xml file",
            tags = { "CSV Imports Download as Liquibase" })
    @ApiResponse(responseCode = "200", description = "Found the table",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ByteArrayResource.class)) })
    @ApiResponse(responseCode = "204", description = "Table data not found")
    public ResponseEntity<?> downloadLiquibaseXmlFile(@RequestParam(value = "tableName") String tableName, @RequestParam(value = "whereCondition", required = false) String whereCondition) {
        try {
            String liquibaseScripts = csvImportUtils.getLiquibaseScripts(tableName, whereCondition);

            if (StringUtils.isNotBlank(liquibaseScripts)) {

                // Example: Generate or fetch the file content
                byte[] fileContent = liquibaseScripts.getBytes();

                // Create a ByteArrayResource from the byte array
                ByteArrayResource resource = new ByteArrayResource(fileContent);

                // Set the filename or other properties as needed
                String filename = tableName + ".xml";

                // Build the response entity
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM) // or use the appropriate media type
                        .contentLength(fileContent.length)
                        .body(resource);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (CsvImportServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
