package com.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.entity.EmailOtp;
import com.repo.EmailOtpRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class OtpService {

    @Autowired private EmailOtpRepository repo;
    @Autowired private JavaMailSender mailSender;

    // ─── Send OTP ────────────────────────────────────────────────────────────
    public void sendOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        EmailOtp data = new EmailOtp();
        data.setEmail(email);
        data.setOtp(otp);
        data.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        repo.save(data);
        sendHtmlEmail(email, otp, "🔐 StegnoVault Verification Code", buildOtpHtml(otp, "Registration Verification"));
    }

    // ─── Send forgot-password OTP ─────────────────────────────────────────────
    public void sendForgotPasswordOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        EmailOtp data = new EmailOtp();
        data.setEmail(email);
        data.setOtp(otp);
        data.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        repo.save(data);
        sendHtmlEmail(email, otp, "🔑 StegnoVault Password Reset", buildOtpHtml(otp, "Password Reset"));
    }

    // ─── Verify OTP ──────────────────────────────────────────────────────────
    public boolean verifyOtp(String email, String otp) {
        EmailOtp data = repo.findById(email).orElse(null);
        if (data == null) return false;
        if (data.getExpiryTime().isBefore(LocalDateTime.now())) { repo.deleteById(email); return false; }
        return data.getOtp().equals(otp);
    }

    public void clearOtp(String email) { try { repo.deleteById(email); } catch (Exception ignored) {} }

    // ─── HTML helpers ─────────────────────────────────────────────────────────
    private String buildOtpHtml(String otp, String purpose) {
        return "<html><body style='background:#050505;margin:0;padding:40px;font-family:sans-serif;'>"
            + "<div style='max-width:500px;margin:0 auto;background:#0a0a0a;border:1px solid #1a1a1a;border-radius:20px;padding:40px;text-align:center;color:white;'>"
            + "<h1 style='color:#00f5ff;font-size:24px;letter-spacing:4px;'>STEGNOVAULT</h1>"
            + "<p style='color:#888;font-size:13px;margin-bottom:24px;'>SECURITY PROTOCOL: " + purpose.toUpperCase() + "</p>"
            + "<div style='background:rgba(255,255,255,0.03);border:1px solid rgba(0,245,255,0.2);border-radius:12px;padding:30px;margin-bottom:24px;'>"
            + "<p style='font-size:15px;color:#ccc;'>Your " + purpose + " code:</p>"
            + "<h2 style='color:#00f5ff;font-size:48px;letter-spacing:12px;font-family:monospace;'>" + otp + "</h2>"
            + "<p style='font-size:13px;color:#666;'>Valid for <strong>10 minutes</strong> only.</p>"
            + "</div>"
            + "<p style='font-size:11px;color:#444;'>If you did not request this code, please ignore this message.<br>© 2026 StegnoVault Security Systems</p>"
            + "</div></body></html>";
    }

    private void sendHtmlEmail(String email, String otp, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
