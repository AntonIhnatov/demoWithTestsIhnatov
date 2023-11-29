package com.example.demowithtests.util.annotations.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NameNotAllowedFormedValidator.class)
public @interface NameNotAllowedFormed {

    String message() default "Name contains a restricted value";

    String[] contains() default {"Putin", "Hitler", "Stalin", "Beria", "Osama bin Laden", "Pol Pot",
            "Kim Chen In", "Mao Zedong", "Albert Fish", "Jeffrey Dahmer"};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
