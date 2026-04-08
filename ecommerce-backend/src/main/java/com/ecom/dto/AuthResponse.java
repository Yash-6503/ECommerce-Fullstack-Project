package com.ecom.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}