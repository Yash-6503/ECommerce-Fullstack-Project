package com.ecom.serviceImpl;

import org.springframework.stereotype.Service;

import com.ecom.service.SmsService;

@Service
public class SmsServiceImpl implements SmsService {

	@Override
	public void sendSms(String phone, String otp) {
        System.out.println("Sending OTP to phone: " + phone + " OTP: " + otp);
    }

}
