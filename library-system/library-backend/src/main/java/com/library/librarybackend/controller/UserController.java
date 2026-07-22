package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.User;
import com.library.librarybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Handles all user-related HTTP requests
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private UserService userService;

    // GET /users/me - returns the logged-in borrower's own profile
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getProfile(Authentication auth) {
        try {
           String userId = (String) auth.getPrincipal();
           return userService.getUserById(userId)
                   .map( u -> {
                       u.setPasswordHash(null);
                       return ResponseEntity.ok(ApiResponse.ok("OK", u));
                   })
                   .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // PUT /users/me - borrower updates their own profile info
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @RequestBody Map<String, String> body, Authentication auth) {
        try {
            String userId = (String) auth.getPrincipal();
            userService.updateProfile(userId, body.get("fullName"), body.get("phone"));
            return ResponseEntity.ok(ApiResponse.ok("Profile updated", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /users/admin/all - returns every user in the system
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> allUsers() {
        try {
            List<User> users = userService.getAllUsers();
            users.forEach(u -> u.setPasswordHash(null));
            return ResponseEntity.ok(ApiResponse.ok("OK", users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET/users/admin/pending - returns accounts waiting for approval
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> pendingUsers(){
        try {
            List<User> users = userService.getPendingUsers();
            users.forEach(u -> u.setPasswordHash(null));
            return ResponseEntity.ok(ApiResponse.ok("OK", users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /users/admin/{userId}/approve - admin approves a pending borrower account
    @PostMapping("/admin/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> approveUser(@PathVariable String userId) {
        try {
            User user = userService.approveUser(userId);
            user.setPasswordHash(null);
            return ResponseEntity.ok(ApiResponse.ok("User approved", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /users/admin/{userId}/suspend - admin suspends a borrower account
    @PostMapping("/admin/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> suspendUser(@PathVariable String userId) {
        try {
            User user = userService.suspendUser(userId);
            user.setPasswordHash(null);
            return ResponseEntity.ok(ApiResponse.ok("User suspended", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /users/admin/{userId}/reactivate - admin reactivates a suspended account
    @PostMapping("/admin/{userId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> reactivateUser(@PathVariable String userId) {
        try {
            User user = userService.reactivateUser(userId);
            user.setPasswordHash(null);
            return ResponseEntity.ok(ApiResponse.ok("User reactivated", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}
