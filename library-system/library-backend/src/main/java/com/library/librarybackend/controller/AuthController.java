package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.dto.AuthRequest;
import com.library.librarybackend.dto.AuthResponse;
import com.library.librarybackend.dto.RegisterRequest;
import com.library.librarybackend.model.User;
import com.library.librarybackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Handles login and registration
// Separate from UserController because auth is a different concern
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthService authService;

    // POST /auth/register - borrower creates their own account.
    // Account starts as pending - admin must approve before they can log in
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterRequest req) {
        try {
            User user = authService.register(req);
            // Never send the password hash back to the client
            user.setPasswordHash(null);
            return ResponseEntity.ok(ApiResponse.ok(
                    "Registration successful. Please wait for the librarian to approve your account.",user
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /auth/login - works for both borrowers and admin
    // Returns a JWT token the client stores and sends on every subsequent request
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest req) {
        try {
            AuthResponse resp = authService.login(req);
            return ResponseEntity.ok(ApiResponse.ok("Login success", resp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /auth/create-admin - admin creates another admin account
    // Only callable by an existing admin via the Android app
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> createAdmin(@RequestBody RegisterRequest req) {
        try {
            User admin = authService.createAdmin(req);
            admin.setPasswordHash(null);
            return ResponseEntity.ok(ApiResponse.ok("Admin account created", admin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Post /auth/bootstrap-admin - one-time endpoint to create the admin the very first admin account
    @PostMapping("/bootstrap-admin")
    public ResponseEntity<ApiResponse<User>> bootstrapAdmin(@RequestBody RegisterRequest req)  {
        try {
            User admin = authService.bootstrapFirstAdmin(req);
            admin.setPasswordHash(null);
            return ResponseEntity.ok(ApiResponse.ok(
                    "First admin created. This endpoint is now locked.", admin));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}
