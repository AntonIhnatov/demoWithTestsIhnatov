package com.example.demowithtests.dto;

public record EmployeeUAWomenDto(
        String message,
        int count
) {
    public EmployeeUAWomenDto(
            String message,
            int count) {
        this.count = count;
        this.message = message;
    }
}
