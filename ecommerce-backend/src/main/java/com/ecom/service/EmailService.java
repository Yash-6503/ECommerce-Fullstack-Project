package com.ecom.service;

import jakarta.mail.MessagingException;

public interface EmailService {
	public void sendEmail(String to, String otp) throws MessagingException;
	public void sendWelcomeEmail(String to);
}
