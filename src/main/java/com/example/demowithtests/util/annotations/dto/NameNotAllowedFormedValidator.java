package com.example.demowithtests.util.annotations.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class NameNotAllowedFormedValidator implements ConstraintValidator<NameNotAllowedFormed, String> {

    private String[] restrictedNames;


    @Override
    public void initialize(NameNotAllowedFormed constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        restrictedNames = constraintAnnotation.contains();
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return name == null || Arrays.stream(restrictedNames).noneMatch(name.trim()::equalsIgnoreCase);
    }
}
