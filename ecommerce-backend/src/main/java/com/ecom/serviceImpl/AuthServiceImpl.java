package com.ecom.serviceImpl;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecom.configurations.JwtUtil;
import com.ecom.dto.AuthResponse;
import com.ecom.dto.LoginRequest;
import com.ecom.dto.RefreshTokenRequest;
import com.ecom.dto.RegisterRequest;
import com.ecom.entity.Customer;
import com.ecom.entity.OtpVerification;
import com.ecom.entity.RefreshToken;
import com.ecom.enums.Role;
import com.ecom.exceptions.InvalidIdentifierException;
import com.ecom.exceptions.InvalidOtpException;
import com.ecom.exceptions.InvalidPasswordException;
import com.ecom.exceptions.InvalidRefreshToken;
import com.ecom.exceptions.InvalidTokenException;
import com.ecom.exceptions.OtpExpiredException;
import com.ecom.exceptions.OtpNotFoundException;
import com.ecom.exceptions.OtpNotVerifiedException;
import com.ecom.exceptions.OtpSendFailedException;
import com.ecom.exceptions.PasswordMismatchException;
import com.ecom.exceptions.UserAlreadyExistsException;
import com.ecom.exceptions.UserNotFoundException;
import com.ecom.repository.CustomerRepository;
import com.ecom.repository.OtpRepository;
import com.ecom.repository.RefreshTokenRepository;
import com.ecom.service.AuthService;
import com.ecom.service.EmailService;
import com.ecom.service.SmsService;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
//        boolean isPhone = identifier.matches("\\d{10}");

        // ✅ Validate format
        if (isEmail && !identifier.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidIdentifierException("Invalid email format");
        }

        if(!isEmail) {
        	throw new InvalidIdentifierException("Invalid email format. Must be ends with @gmail.com");        	
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
//        if (!isEmail && !identifier.matches("\\d{10}")) {
//            throw new InvalidIdentifierException("Invalid phone number. Must be 10 digits");
//        }

        // ✅ Check if user already exists
        if (isEmail && customerRepository.findByEmail(identifier).isPresent()) {
            throw new UserAlreadyExistsException("User already registered with this email");
        }

        if (!isEmail && customerRepository.findByPhone(identifier).isPresent()) {
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

        Customer customer = new Customer();

        if (isEmail) {
            customer.setEmail(identifier);
        } else {
            customer.setPhone(identifier);
        }

        customer.setPassword(passwordEncoder.encode(request.password()));
        customer.setRole(Role.CUSTOMER);

        customerRepository.save(customer);

        // ✅ Send Welcome Email
        if (customer.getEmail() != null) {
            emailService.sendWelcomeEmail(customer.getEmail());
        }

        return "User registered successfully";
    }
    
   
    //login
    public AuthResponse login(LoginRequest request) {

        Customer user = null;

        if (request.identifier().contains("@")) {
            user = customerRepository.findByEmail(request.identifier())
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        } 
//        else {
//            user = userRepository.findByPhone(request.identifier())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }

        String accessToken = jwtUtil.generateAccessToken(request.identifier());
        String refreshToken = jwtUtil.generateRefreshToken(request.identifier());

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUsername(request.identifier());
        tokenEntity.setExpiryDate(LocalDateTime.now().plusDays(1));

        refreshTokenRepository.save(tokenEntity);

        return new AuthResponse(accessToken, refreshToken);
    }
    
    
    //refresh token
    public AuthResponse refreshToken(RefreshTokenRequest refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken.refreshToken())
                .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        String newAccessToken = jwtUtil.generateAccessToken(token.getUsername());

        return new AuthResponse(newAccessToken, refreshToken.refreshToken());
    }
    
    //logout
    public String logout(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshToken("Invalid refresh token"));

        refreshTokenRepository.delete(token);

        return "Logged out successfully";
    }
    
}