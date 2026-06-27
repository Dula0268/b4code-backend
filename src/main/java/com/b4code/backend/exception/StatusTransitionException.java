package com.b4code.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StatusTransitionException extends RuntimeException {
    public StatusTransitionException(String message) {
        super(message);
    }
}

