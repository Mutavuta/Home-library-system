package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.User;
import com.library.librarybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Handles login and registration
// Separate from UserController because auth is a different concern
@RestController
@RequestMapping("/auth")
public class AuthController {

    // Spring injects UserService
    @Autowired
    private UserService userService;

    // POST /auth/register - new borrower creates an account
    // Status is forced to "pending" in UserController
    // Role is forced to "borrower" in UserController
    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody User user) {

        // Block registration if email already exists
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ApiResponse.error("Email already registered");
        }

        // Validate required fields are not empty
        if (user.getFullName() == null || user.getEmail() == null ||
        user.getPasswordHash() == null){
            return ApiResponse.error("Name, email and password are required");
        }

        User created = userService.createUser(user);

        // Never send password hash back to the client
        created.setPasswordHash(null);

        return ApiResponse.ok("Registration successful. " +
                "Your account is pending approval. ",created);
    }

    //POST /auth/login - user logs in with email and password
    // A simple match for now - later add BCrypt + JWT
    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody Map<String, String> body) {

        String email    = body.get("email");
        String password = body.get("password");

        // Validate fields present
        if (email == null || password == null) {
            return ApiResponse.error("Email and password are required");
        }

        // Find user by email
        // orElse returns null if not found
        User user = userService.findByEmail(email).orElse(null);

        // Email not found
        if (user == null) {
            // Deliberately vague - do not tell attacker whether email exists
            return ApiResponse.error("Invalid email or password");
        }

        // Check password matches
        // Simple string comparison for now - replaced with Bcrypt later
        if ( ! password.equals(user.getPasswordHash())) {
            return ApiResponse.error("Invalid email or password");
        }

        // Block suspended users from logging in
        if ("suspend".equals(user.getStatus())) {
            return ApiResponse.error("Account suspended. Contact the librarian.");
        }

        // Block pending users from logging in
        if ("pending".equals(user.getStatus())) {
            return ApiResponse.error("Account pending approval. " +
                    "Wait for the librarian to approve your account. ");
        }

        // Never send password hash back to the client
        user.setPasswordHash(null);

        return ApiResponse.ok("Login successful", user);
    }

}
