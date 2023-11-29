package com.example.demowithtests.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class EmployeeExceptionHandler {

    @ExceptionHandler(GenderNotFoundException.class)
    protected ResponseEntity<?> controlGenderNotFoundException(GenderNotFoundException exception){
        var response = new EmployeeErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Gender value not recorded",
                "Gender value - " + exception.getMessage() + "not exist",
                LocalDateTime.now()
        );
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CountryNotSpecifiedException.class)
    protected ResponseEntity<?> handleCountryNotSpecifiedException(CountryNotSpecifiedException exception){
        var response = new EmployeeErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Country not specified",
                "Country value - " + exception.getMessage() + "You can specify your country in your document",
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
