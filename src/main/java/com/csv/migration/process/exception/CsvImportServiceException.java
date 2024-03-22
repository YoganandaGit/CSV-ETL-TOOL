package com.csv.migration.process.exception;

public class CsvImportServiceException extends Exception {
    public CsvImportServiceException(String message) {
        super(message);
    }

    public CsvImportServiceException(Throwable cause) {
        super(cause);
    }

    public CsvImportServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
