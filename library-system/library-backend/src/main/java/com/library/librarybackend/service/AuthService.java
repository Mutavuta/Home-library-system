package com.library.librarybackend.service;

import com.library.librarybackend.dto.AuthRequest;
import com.library.librarybackend.dto.AuthResponse;
import com.library.librarybackend.dto.RegisterRequest;
import com.library.librarybackend.model.User;
import com.library.librarybackend.repository.UserRepository;
import com.library.librarybackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Handles registration and login for both borrowers and admins
@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    // BCrypt PasswordEncoder injected from SecurityConfig
    @Autowired private PasswordEncoder passwordEncoder;

    // Registers a new borrower account - role = borrower and status = pending
    // Admin must approve before the borrower can log in successfully
    public User register(RegisterRequest req) throws ExecutionException, InterruptedException {
        // Block duplicate emails - email should be unique across the system
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }
        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("borrower");
        user.setStatus("pending");
        // Hash the plain-text password before storing
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setCreatedAt(LocalDate.now().toString());
        return userRepository.save(user);
    }

    // Logs in a user and returns a JWT token
    // Works for both borrowers and admins
    public AuthResponse login(AuthRequest req) throws ExecutionException, InterruptedException {
        // Look up user by email - throws if not found
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        // Compare the submitted password against the stored hash
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // Suspended accounts can't log in
        if ("suspended".equals(user.getStatus()) || "pending".equals(user.getStatus())) {
            throw new RuntimeException ("Your account has been suspended. Please contact the librarian. ");
        }

        // Generate a signed JWT token containing userId, email and role
        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getRole());

        // Return token and user info
        return new AuthResponse(token, user.getId(), user.getRole(),
                user.getFullName(), user.getStatus());
    }

    // Creates a new admin account
    public User createAdmin (RegisterRequest req) throws ExecutionException, InterruptedException {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }
        return userRepository.save(buildAdmin(req));
    }

    // One-time bootstrap endpoint to create the very first admin when the databse is empty
    public User bootstrapFirstAdmin (RegisterRequest req)
        throws ExecutionException, InterruptedException {
        // Check if any admin already exists in the system
        List<User> allUsers = userRepository.findAll();
        boolean adminExists = allUsers.stream().anyMatch(u -> "admin".equals(u.getRole()));
        if (adminExists) {
            throw new RuntimeException(
                    "An admin account already exists. " +
                            "This endpoint is locked. Use the Android app to manage Admins. "
            );
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }
        return userRepository.save(buildAdmin(req));
    }

    //  Shared helper that builds an admin User object from a RegisterRequest
    public User buildAdmin(RegisterRequest req) {
        User admin = new User();
        admin.setFullName(req.getFullName());
        admin.setEmail(req.getEmail());
        admin.setPhone(req.getPhone());
        admin.setRole("admin");
        admin.setStatus("approved");
        admin.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        admin.setCreatedAt(LocalDate.now().toString());
        return admin;
    }

}
