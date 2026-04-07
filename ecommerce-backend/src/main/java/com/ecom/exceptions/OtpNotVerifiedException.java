package com.ecom.exceptions;

public class OtpNotVerifiedException extends RuntimeException {
    public OtpNotVerifiedException(String message) {
        super(message);
    }
}