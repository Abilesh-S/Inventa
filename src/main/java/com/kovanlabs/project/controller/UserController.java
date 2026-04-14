package com.kovanlabs.project.controller;

import com.kovanlabs.project.dto.AuthResponseDTO;
import com.kovanlabs.project.dto.LoginDTO;
import com.kovanlabs.project.dto.UserDTO;
import com.kovanlabs.project.model.Role;
import com.kovanlabs.project.model.User;
import com.kovanlabs.project.repository.UserRepository;
import com.kovanlabs.project.security.JwtService;
import com.kovanlabs.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final com.kovanlabs.project.repository.UserRepository userRepository;
    private final com.kovanlabs.project.service.EmailService emailService;

    public UserController(UserService userService, JwtService jwtService,
                          com.kovanlabs.project.repository.UserRepository userRepository,
                          com.kovanlabs.project.service.EmailService emailService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> emailVerification(@RequestBody com.kovanlabs.project.dto.OwnerDTO dto) {
        emailService.sendVerificationEmail(dto.getEmailId());
        return ResponseEntity.ok("Verification email sent successfully");
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@RequestBody com.kovanlabs.project.dto.OwnerDTO dto) {
        emailService.validateVerificationEmail(dto.getEmailId(), dto.getEmailOtp());
        return ResponseEntity.ok("OTP validated successfully");
    }

    @GetMapping
    public List<User> getAllUsers(Authentication auth) {
        String email = auth.getName();
        User user = userService.findByEmail(email);
        if (user == null || user.getBusiness() == null) {
            throw new RuntimeException("Current user context invalid");
        }
        return userService.getAllUsers(user.getBusiness().getId());
    }

    @PostMapping("/register-owner")
    public Object registerOwner(@RequestBody UserDTO dto) {
        return userService.registerOwner(dto, Role.OWNER);
    }

    @PostMapping("/login-owner")
    public User loginOwner(@RequestBody LoginDTO dto) {
        return userService.login(dto);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginDTO dto) {
        return userService.loginAnyRole(dto);
    }

    @PostMapping("/login-jwt")
    public AuthResponseDTO loginJwt(@RequestBody LoginDTO dto) {
        User user = userService.loginAnyRole(dto);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponseDTO(user, token);
    }

    @PostMapping("/create-manager")
    public User createManager(@RequestBody UserDTO dto, Authentication authentication) {
        return userService.registerBranchUserByOwner(dto, Role.MANAGER, authentication.getName());
    }

    @PostMapping("/create-staff")
    public User createStaff(@RequestBody UserDTO dto, Authentication authentication) {
        User caller = userService.findByEmail(authentication.getName());
        // If a manager is creating staff, auto-assign to their branch
        if (caller != null && caller.getRole() == Role.MANAGER && caller.getBranch() != null) {
            if (dto.getBranchId() == null) {
                dto.setBranchId(caller.getBranch().getId());
            }
            if (dto.getBusinessId() == null && caller.getBusiness() != null) {
                dto.setBusinessId(caller.getBusiness().getId());
            }
        }
        return userService.registerBranchUserByOwner(dto, Role.STAFF, authentication.getName());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<?> getUsersByBranch(@PathVariable Long branchId, Authentication authentication) {
        User caller = userService.findByEmail(authentication.getName());
        if (caller == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");

        if (caller.getRole() == Role.MANAGER) {
            if (caller.getBranch() == null || !caller.getBranch().getId().equals(branchId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
        } else if (caller.getRole() != Role.OWNER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        List<User> managers = userRepository.findByBranchIdAndRole(branchId, Role.MANAGER);
        List<User> staff    = userRepository.findByBranchIdAndRole(branchId, Role.STAFF);
        return ResponseEntity.ok(Map.of("managers", managers, "staff", staff));
    }
}