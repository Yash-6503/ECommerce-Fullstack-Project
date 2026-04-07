package com.ecom.exceptions;

public class OtpSendFailedException extends RuntimeException {
    public OtpSendFailedException(String message) {
        super(message);
    }
}