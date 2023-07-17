package com.example.accountservice.exception;

public class ExistingEntityCreationException extends RuntimeException{

    public ExistingEntityCreationException(String message) {
        super(message);
    }

}
