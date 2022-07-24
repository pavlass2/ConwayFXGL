package com.michalec.conwayFXGL.entity;

public class DataMalformedException extends Exception {
    long recordNumber;
    private String malformedData;
    private String fileName;
    public DataMalformedException(long recordNumber, String malformedData, String fileName) {
        super();
        this.recordNumber = recordNumber;
        this.malformedData = malformedData;
        this.fileName = fileName;
    }

    public long getRecordNumber() {
        return recordNumber;
    }

    public String getMalformedData() {
        return malformedData;
    }

    public String getFileName() {
        return fileName;
    }
}
