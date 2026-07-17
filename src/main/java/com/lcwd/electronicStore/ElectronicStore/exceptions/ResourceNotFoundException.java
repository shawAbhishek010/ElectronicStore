package com.lcwd.electronicStore.ElectronicStore.exceptions;

/*
Purpose:
Signals missing database resources that should return a 404 response.
*/
import lombok.Builder;

@Builder
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Resource not found!");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
