package com.kovanlabs.project.service;

import com.kovanlabs.project.exception.PasswordVerifyEmailException;
import com.kovanlabs.project.model.EmailVerification;
import com.kovanlabs.project.repository.EmailVerificationRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Value("${email.sender}")
    private String senderEmail;

    /** Sends a 6-digit OTP for email verification (registration flow) */
    @Transactional
    public void sendVerificationEmail(String email) {
        try {
            String otp = generateOtp();
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("Ventorie — Email Verification");
            helper.setText(
                    "<h2>Welcome to Ventorie</h2>" +
                            "<p>Your email verification OTP is: <strong>" + otp + "</strong></p>" +
                            "<p>This code is valid for 10 minutes.</p>",
                    true
            );

            EmailVerification ev = emailVerificationRepository.findByEmailId(email)
                    .orElse(new EmailVerification());
            ev.setEmailId(email);
            ev.setOneTimePassword(otp);
            ev.setVerified(false);
            emailVerificationRepository.save(ev);

            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PasswordVerifyEmailException("Error sending verification email: " + e.getMessage());
        }
    }

    /** Validates the OTP entered by the user */
    public void validateVerificationEmail(String emailId, String otp) {
        EmailVerification ev = emailVerificationRepository.findByEmailId(emailId)
                .orElseThrow(() -> new RuntimeException("No verification record found for this email."));

        if (!ev.getOneTimePassword().equals(otp)) {
            throw new RuntimeException("Invalid OTP. Please try again.");
        }
        ev.setVerified(true);
        emailVerificationRepository.save(ev);
    }

    /** Checks if the email has been verified */
    public boolean isEmailVerified(String email) {
        return emailVerificationRepository.findByEmailId(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    /**
     * Sends a temporary OTP password when owner/manager creates an account for someone.
     * Returns the OTP so the caller can encode it as the user's initial password.
     */
    @Transactional
    public String sendAccountCreationOtp(String email, String recipientName) {
        try {
            String otp = generateOtp();
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("Welcome to Ventorie — Your Account is Ready");
            helper.setText(
                    "<h2>Welcome to Ventorie, " + recipientName + "!</h2>" +
                            "<p>Your account has been created. Use the OTP below to log in for the first time:</p>" +
                            "<h3 style='letter-spacing:8px;font-size:32px;color:#496400'>" + otp + "</h3>" +
                            "<p><strong>Important:</strong> Please change your password after your first login.</p>",
                    true
            );
            javaMailSender.send(message);
            return otp;
        } catch (Exception e) {
            e.printStackTrace();
            throw new PasswordVerifyEmailException("Error sending account creation email: " + e.getMessage());
        }
    }

    /** Sends a password reset OTP */
    @Transactional
    public void sendPasswordResetOtp(String email) {
        try {
            String otp = generateOtp();
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject("Ventorie — Password Reset OTP");
            helper.setText(
                    "<h2>Password Reset Request</h2>" +
                            "<p>Your OTP for password reset is: <strong>" + otp + "</strong></p>" +
                            "<p>If you did not request this, please ignore this email.</p>",
                    true
            );

            EmailVerification ev = emailVerificationRepository.findByEmailId(email)
                    .orElse(new EmailVerification());
            ev.setEmailId(email);
            ev.setOneTimePassword(otp);
            ev.setVerified(false);
            emailVerificationRepository.save(ev);

            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PasswordVerifyEmailException("Error sending password reset email: " + e.getMessage());
        }
    }

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}
