package com.example.demowithtests.util.annotations.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NameNoNumbersFormedValidator.class)
public @interface NameNoNumbersFormed {

    String message() default "The name field must not contain numbers";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
