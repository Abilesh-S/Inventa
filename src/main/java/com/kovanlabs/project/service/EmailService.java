package com.kovanlabs.project.service;

import com.kovanlabs.project.exception.PasswordVerifyEmailException;
import com.kovanlabs.project.model.EmailVerification;
import com.kovanlabs.project.repository.EmailVerificationRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    @Transactional
    public void sendVerificationEmail(String email) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom("abilesh1545@gmail.com");
            messageHelper.setTo(email);
            messageHelper.setSubject("Email Validation :");
            String oneTimePassword = String.valueOf((int)(Math.random() * 900000) + 100000);
            messageHelper.setText(
                    "<h2>Welcome to Ventorie</h2>" +
                            "<p> One Time Password for the Account Creation is "+oneTimePassword+" </p>",
                    true);

            EmailVerification emailVerification = emailVerificationRepository.findByEmailId(email)
                    .orElse(new EmailVerification());

            emailVerification.setEmailId(email);
            emailVerification.setOneTimePassword(oneTimePassword);
            emailVerification.setVerified(false); // Reset verified status on new OTP
            emailVerificationRepository.save(emailVerification);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new PasswordVerifyEmailException("Error Occurred during email sending: " + e.getMessage());
        }
    }

    public void validateVerificationEmail(String emailId, String otp) {
        EmailVerification emailVerification = emailVerificationRepository.findByEmailId(emailId)
                .orElseThrow(() -> new RuntimeException("Email verification record not found."));

        if (emailVerification.getOneTimePassword().equals(otp)) {
            emailVerification.setVerified(true);
            emailVerificationRepository.save(emailVerification);
        } else {
            throw new RuntimeException("Invalid verification code. Please try again.");
        }
    }
    @Transactional
    public String oneTimePasswordforAccountCreation(String email){
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom("abilesh1545@gmail.com");
            messageHelper.setTo(email);
            messageHelper.setSubject("Welcome To Ventorie :");
            String oneTimePassword = String.valueOf((int)(Math.random() * 900000) + 100000);
            messageHelper.setText(
                    "<h2>Welcome to Ventorie</h2>" +
                            "<p> Your Password for you account is "+oneTimePassword+" </p><br/><p>Please Make sure you change password after login</p>",
                    true);

            javaMailSender.send(message);
            return oneTimePassword;
        } catch (Exception e) {
            e.printStackTrace();
            throw new PasswordVerifyEmailException("Error Occurred during email sending: " + e.getMessage());
        }
    }


}
