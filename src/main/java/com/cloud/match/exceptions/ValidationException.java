package com.cloud.match.exceptions;

import java.io.Serial;

public class ValidationException extends RuntimeException{
    @Serial
    private static final long serialVersionUID = -5708758101990690675L;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException() {
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

}
