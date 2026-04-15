package com.example.telematicsscanner.model;

public class DiagnosticCode {
    private String code;
    private String description;

    public DiagnosticCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}