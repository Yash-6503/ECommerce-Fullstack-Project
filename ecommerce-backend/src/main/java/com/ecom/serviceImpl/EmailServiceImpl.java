package com.ecom.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.ecom.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String otp) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("🔐 ShopKaro OTP Verification");

        String htmlContent = "<div style='font-family: Arial, sans-serif; background-color:#f5f7fa; padding:20px;'>"

                + "<div style='max-width:600px; margin:auto; background:#ffffff; border-radius:12px; overflow:hidden; "
                + "box-shadow:0 4px 12px rgba(0,0,0,0.1);'>"

                // 🔶 Header
                + "<div style='background:#ff6f00; padding:20px; text-align:center; color:white;'>"
                + "<h1 style='margin:0;'>🛒 ShopKaro</h1>"
                + "<p style='margin:5px 0 0; font-size:14px;'>Your Trusted Shopping Partner</p>"
                + "</div>"

                // 🔹 Body
                + "<div style='padding:25px;'>"
                + "<h2 style='color:#333;'>🔐 OTP Verification</h2>"

                // ✅ FIXED LINE
                + "<p style='font-size:16px; color:#555;'>Hello, <b>" + to + "</b></p>"

                + "<p style='font-size:16px; color:#555;'>We received a request to verify your account on "
                + "<b>ShopKaro</b>.</p>"

                + "<p style='font-size:16px; color:#555;'>Use the OTP below to continue:</p>"

                // 🔐 OTP Box
                + "<div style='text-align:center; margin:25px 0;'>"
                + "<span style='display:inline-block; font-size:28px; letter-spacing:6px; "
                + "background:#ff6f00; color:white; padding:12px 25px; border-radius:8px; "
                + "font-weight:bold;'>"
                + otp
                + "</span>"
                + "</div>"

                + "<p style='font-size:14px; color:#777;'>⏳ This OTP is valid for <b>5 minutes</b>.</p>"
                + "<p style='font-size:14px; color:#777;'>🔒 Do not share this OTP with anyone.</p>"

                + "<hr style='margin:25px 0;'>"

                + "<p style='font-size:13px; color:#999;'>If you did not request this, please ignore this email.</p>"

                + "</div>"

                // 🔻 Footer
                + "<div style='background:#f1f1f1; padding:15px; text-align:center;'>"
                + "<p style='font-size:12px; color:#777;'>© 2026 ShopKaro E-Commerce Website</p>"
                + "<p style='font-size:12px; color:#777;'>Made with ❤️ for a better shopping experience</p>"
                + "</div>"

                + "</div>"
                + "</div>";

        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
    
	@Override
	public void sendWelcomeEmail(String to) {

	    try {
	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);

	        helper.setTo(to);
	        helper.setSubject("🎉 Welcome to ShopKaro - Let’s Start Shopping!");

	        String htmlContent = "<div style='font-family: Arial, sans-serif; background-color:#f5f7fa; padding:20px;'>"
	                + "<div style='max-width:600px; margin:auto; background:white; border-radius:12px; overflow:hidden; box-shadow:0 4px 10px rgba(0,0,0,0.1);'>"

	                // Header
	                + "<div style='background:#ff6f00; padding:20px; text-align:center; color:white;'>"
	                + "<h1 style='margin:0;'>🛒 ShopKaro</h1>"
	                + "<p style='margin:5px 0 0;'>Your Favorite E-Commerce Destination</p>"
	                + "</div>"

	                // Body
	                + "<div style='padding:25px;'>"
	                + "<h2 style='color:#333;'>🎉 Welcome to ShopKaro!</h2>"
	                // ✅ FIXED LINE
	                + "<p style='font-size:16px; color:#555;'>Hello, <b>" + to + "</b></p>"
	                + "<p style='font-size:16px; color:#555;'>We're thrilled to have you join the <b>ShopKaro</b> family! Your account has been successfully created.</p>"

	                + "<p style='font-size:16px; color:#555;'>Now you can explore a wide range of products, enjoy great deals, and experience seamless shopping.</p>"

	                // CTA Button
	                + "<div style='text-align:center; margin:30px 0;'>"
	                + "<a href='#' style='background:#ff6f00; color:white; padding:12px 25px; font-size:16px; "
	                + "text-decoration:none; border-radius:6px; display:inline-block;'>Start Shopping 🛍️</a>"
	                + "</div>"

	                // Features Section
	                + "<div style='margin-top:20px;'>"
	                + "<p style='font-size:15px; color:#333;'><b>What you can do:</b></p>"
	                + "<ul style='color:#555; font-size:14px;'>"
	                + "<li>🛍️ Browse thousands of products</li>"
	                + "<li>🔥 Get exclusive deals & discounts</li>"
	                + "<li>🚚 Fast & reliable delivery</li>"
	                + "<li>💳 Secure payments</li>"
	                + "</ul>"
	                + "</div>"
	                + "<p style='font-size:14px; color:#777; margin-top:20px;'>If you have any questions, feel free to contact our support team anytime.</p>"
	                + "</div>"

	                // Footer
	                + "<div style='background:#f1f1f1; padding:15px; text-align:center;'>"
	                + "<p style='font-size:12px; color:#777;'>© 2026 ShopKaro E-Commerce Website</p>"
	                + "<p style='font-size:12px; color:#777;'>Made with ❤️ for better shopping experience</p>"
	                + "</div>"

	                + "</div>"
	                + "</div>";

	        helper.setText(htmlContent, true);

	        mailSender.send(message);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
