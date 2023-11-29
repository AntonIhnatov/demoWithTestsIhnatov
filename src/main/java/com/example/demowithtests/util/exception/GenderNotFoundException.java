package com.example.demowithtests.util.exception;

public class GenderNotFoundException extends RuntimeException{
    public GenderNotFoundException(String message){
        super(message);
    }
}
