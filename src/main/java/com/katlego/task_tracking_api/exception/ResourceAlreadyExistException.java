package com.katlego.task_tracking_api.exception;

public class ResourceAlreadyExistException extends RuntimeException {
    public ResourceAlreadyExistException(String message) {
        super(message);
    }
}
