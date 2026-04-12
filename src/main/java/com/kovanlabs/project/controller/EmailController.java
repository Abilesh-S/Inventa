package com.kovanlabs.project.controller;

import com.kovanlabs.project.service.EmailService;
import com.kovanlabs.project.service.UserService;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin("http://localhost:5173")
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmailController(EmailService emailService, UserService userService,
                           UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Step 1 of registration: send OTP to verify email before creating account */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        emailService.sendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("message", "OTP sent to " + email));
    }

    /** Step 2 of registration: verify OTP */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp   = body.get("otp");
        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body("Email and OTP are required");
        }
        emailService.validateVerificationEmail(email, otp);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    /** Forgot password: send reset OTP */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        // Check user exists
        userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("No account found with this email"));
        emailService.sendPasswordResetOtp(email.trim());
        return ResponseEntity.ok(Map.of("message", "Password reset OTP sent to " + email));
    }

    /** Reset password: verify OTP then set new password */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email       = body.get("email");
        String otp         = body.get("otp");
        String newPassword = body.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Email, OTP and new password are required");
        }

        // Validate OTP
        emailService.validateVerificationEmail(email.trim(), otp);

        // Update password
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
                                            org.springframework.security.core.Authentication authentication) {
        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Current and new password are required");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(401).body("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
