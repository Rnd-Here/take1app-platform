package com.takeone.auth.exception;

public class OtpExpiredException extends RuntimeException {
    
    public OtpExpiredException(String message) {
        super(message);
    }
    
    public OtpExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
