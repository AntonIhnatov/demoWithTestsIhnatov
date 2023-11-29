package com.example.demowithtests.util.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EmployeeErrorResponse {
    private int errorCode;
    private String message;
    private String details;
    private LocalDateTime timestamp;
}
