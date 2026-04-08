package com.ecom.service;

import com.ecom.dto.AuthResponse;
import com.ecom.dto.LoginRequest;
import com.ecom.dto.RefreshTokenRequest;
import com.ecom.dto.RegisterRequest;

public interface AuthService {
	public String generateOtp();
	public String sendOtp(String identifier);
	public String verifyOtp(String identifier, String otpInput);
	public String register(RegisterRequest request);
	public AuthResponse login(LoginRequest request);
	public AuthResponse refreshToken(RefreshTokenRequest refreshToken);
	public String logout(String username);
}
