package com.example.demowithtests.util.annotations.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class NameNoNumbersFormedValidator implements ConstraintValidator<NameNoNumbersFormed, String> {


    @Override
    public void initialize(NameNoNumbersFormed constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || !value.matches(".*\\d.*");
    }

}
