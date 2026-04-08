package com.kovanlabs.project.service;

import com.kovanlabs.project.dto.LoginDTO;
import com.kovanlabs.project.dto.UserDTO;
import com.kovanlabs.project.model.*;
import com.kovanlabs.project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,15}$");

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final BranchRepository branchRepository;

    public UserService(UserRepository userRepository,
                       BusinessRepository businessRepository,
                       BranchRepository branchRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.branchRepository = branchRepository;
    }

    public User registerOwner(UserDTO dto, Role role) {
        validateRegistrationFields(dto);

        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found"));

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail().trim());
        user.setPhone(dto.getPhone().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setBusiness(business);
        user.setBranch(null);

        return userRepository.save(user);
    }

    public User login(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.getRole() != Role.OWNER) {
            throw new RuntimeException("Not an owner");
        }

        return user;
    }

    public User loginAnyRole(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    @Transactional
    public User registerBranchUserByOwner(UserDTO dto, Role role, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner user not found"));

        if (owner.getRole() != Role.OWNER) {
            throw new RuntimeException("Only owner can create manager/staff");
        }

        Long businessId = dto.getBusinessId();
        if (businessId == null && owner.getBusiness() != null) {
            businessId = owner.getBusiness().getId();
        }

        if (businessId == null) {
            throw new RuntimeException("businessId is required");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (owner.getBusiness() == null || !owner.getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("Owner can only create users for their own business");
        }

        Branch branch = null;
        if (dto.getBranchId() != null) {
            branch = branchRepository.findById(dto.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found"));

            if (branch.getBusiness() == null || !branch.getBusiness().getId().equals(business.getId())) {
                throw new RuntimeException("Branch does not belong to this business");
            }
        }

        validateRegistrationFields(dto);

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail().trim());
        user.setPhone(dto.getPhone().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setBusiness(business);
        user.setBranch(branch);

        return userRepository.save(user);
    }

    public User createBranchManager(UserDTO dto) {
        validateRegistrationFields(dto);

        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Business not found for ID: " + dto.getBusinessId()));

        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found for ID: " + dto.getBranchId()));

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail().trim());
        user.setPhone(dto.getPhone().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.MANAGER);
        user.setBusiness(business);
        user.setBranch(branch);

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<User> getAllUsers(Long businessId) {
        return userRepository.findByBusinessId(businessId);
    }

    private void validateRegistrationFields(UserDTO dto) {
        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (dto.getPhone() == null || !PHONE_PATTERN.matcher(dto.getPhone().trim()).matches()) {
            throw new IllegalArgumentException("Invalid phone format. Use 10 to 15 digits");
        }
        if (dto.getPassword() == null || !PASSWORD_PATTERN.matcher(dto.getPassword()).matches()) {
            throw new IllegalArgumentException("Invalid password format. Min 8 chars with upper, lower, number, special");
        }

        String normalizedEmail = dto.getEmail().trim();
        String normalizedPhone = dto.getPhone().trim();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new IllegalArgumentException("Phone already exists");
        }
    }
}