package com.ecom.dto;

public record RegisterRequest(
        String identifier,
        String password,
        String confirmPassword,
        String verificationToken
) {}