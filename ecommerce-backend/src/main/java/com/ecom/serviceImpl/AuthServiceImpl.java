package com.ecom.serviceImpl;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecom.dto.RegisterRequest;
import com.ecom.entity.OtpVerification;
import com.ecom.entity.User;
import com.ecom.enums.Role;
import com.ecom.exceptions.InvalidIdentifierException;
import com.ecom.exceptions.InvalidOtpException;
import com.ecom.exceptions.InvalidTokenException;
import com.ecom.exceptions.OtpExpiredException;
import com.ecom.exceptions.OtpNotFoundException;
import com.ecom.exceptions.OtpNotVerifiedException;
import com.ecom.exceptions.OtpSendFailedException;
import com.ecom.exceptions.PasswordMismatchException;
import com.ecom.exceptions.UserAlreadyExistsException;
import com.ecom.repository.OtpRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.AuthService;
import com.ecom.service.EmailService;
import com.ecom.service.SmsService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔹 Generate OTP
    public String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    // 🔹 Send OTP
    public String sendOtp(String identifier) {

        // ✅ Validate input
        if (identifier == null || identifier.isBlank()) {
            throw new InvalidIdentifierException("Email or phone number cannot be empty");
        }

        boolean isEmail = identifier.contains("@gmail.com");
        boolean isPhone = identifier.matches("\\d{10}");

        // ✅ Validate format
        if (isEmail && !identifier.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidIdentifierException("Invalid email format");
        }

        if(!isEmail) {
        	throw new InvalidIdentifierException("Invalid email format. Must be ends with @gmail.com");        	
        }else {
        	if(!isEmail && !isPhone) {
        		throw new InvalidIdentifierException("Invalid phone number. Must be 10 digits");
        	}
        }
        

        String otp = generateOtp();

        OtpVerification otpEntity = otpRepository
                .findByIdentifier(identifier)
                .orElse(new OtpVerification());

        otpEntity.setIdentifier(identifier);
        otpEntity.setOtp(otp);
        otpEntity.setVerified(false);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpEntity);

        // ✅ Send OTP with proper exception handling
        try {
            if (isEmail) {
                emailService.sendEmail(identifier, otp);
            } else {
                smsService.sendSms(identifier, otp);
            }
        } catch (Exception e) {
            throw new OtpSendFailedException("Failed to send OTP. Please try again later.");
        }

        return "OTP sent successfully";
    }
    
    
    // 🔹 Verify OTP
    public String verifyOtp(String identifier, String otpInput) {

        if (identifier == null || identifier.isBlank()) {
            throw new InvalidIdentifierException("Identifier cannot be empty");
        }

        OtpVerification otp = otpRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new OtpNotFoundException("OTP not found for this identifier"));

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP has expired");
        }

        if (!otp.getOtp().equals(otpInput)) {
            throw new InvalidOtpException("Invalid OTP entered");
        }

        otp.setVerified(true);

        String token = UUID.randomUUID().toString();
        otp.setVerificationToken(token);

        otpRepository.save(otp);

        return token;
    }

    // 🔹 Register User
    public String register(RegisterRequest request) {

        String identifier = request.identifier();

        if (identifier == null || identifier.isBlank()) {
            throw new InvalidIdentifierException("Identifier cannot be empty");
        }

        boolean isEmail = identifier.contains("@");

        // ✅ Email Validation FIRST
        if (isEmail && !identifier.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new InvalidIdentifierException("Invalid email format");
        }

        // ✅ Phone Validation
        if (!isEmail && !identifier.matches("\\d{10}")) {
            throw new InvalidIdentifierException("Invalid phone number. Must be 10 digits");
        }

        // ✅ Check if user already exists
        if (isEmail && userRepository.findByEmail(identifier).isPresent()) {
            throw new UserAlreadyExistsException("User already registered with this email");
        }

        if (!isEmail && userRepository.findByPhone(identifier).isPresent()) {
            throw new UserAlreadyExistsException("User already registered with this phone number");
        }

        // ✅ Now check OTP (AFTER validation)
        OtpVerification otp = otpRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new OtpNotFoundException("OTP not found for this identifier"));

        if (!otp.isVerified()) {
            throw new OtpNotVerifiedException("OTP is not verified");
        }

        if (!otp.getVerificationToken().equals(request.verificationToken())) {
            throw new InvalidTokenException("Invalid verification token");
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        User user = new User();

        if (isEmail) {
            user.setEmail(identifier);
        } else {
            user.setPhone(identifier);
        }

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);

        // ✅ Send Welcome Email
        if (user.getEmail() != null) {
            emailService.sendWelcomeEmail(user.getEmail());
        }

        return "User registered successfully";
    }
}