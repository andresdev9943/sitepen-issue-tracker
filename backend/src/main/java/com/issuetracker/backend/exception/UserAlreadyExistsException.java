package com.issuetracker.backend.exception;

public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String email, String field) {
        super(String.format("User with %s '%s' already exists", field, email));
    }
}
