package com.example.authservice.exception;

public class ExistingEntityCreationException extends RuntimeException{

    public ExistingEntityCreationException(String message) {
        super(message);
    }

}
