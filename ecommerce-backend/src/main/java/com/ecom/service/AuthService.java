package com.ecom.service;

import com.ecom.dto.RegisterRequest;

public interface AuthService {
	public String generateOtp();
	public String sendOtp(String identifier);
	public String verifyOtp(String identifier, String otpInput);
	public String register(RegisterRequest request);
	
}
