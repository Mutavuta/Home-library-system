package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.User;
import com.library.librarybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

// Handles all user-related HTTP requests
@RestController
// Declares all requests to be served by this class must start with "/users" in their request path
@RequestMapping("/users")
public class UserController {

    // Spring injects UserService automatically
    @Autowired
    private UserService userService;

    // GET /users - returns all users
    @GetMapping
    public ApiResponse<List<User>> getAllUsers() {
        return ApiResponse.ok("Users fetched", userService.getAllUsers());
    }

    // GET /users/pending - returns pending users
    @GetMapping("/pending")
    public ApiResponse<List<User>> getPendingUsers() {
        return ApiResponse.ok("Pending users",
                userService.getUsersByStatus("pending"));
    }

    // POST /users/register - creates a new borrower account
    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody User user) {

        // Check if email exists before creating
        if  (userService.findByEmail(user.getEmail()).isPresent()) {
            return ApiResponse.error("Email already exists");
        }

        User created = userService.createUser(user);
        // Never send password hash back to client
        created.setPasswordHash(null);
        return ApiResponse.ok("User created", created);
    }

    // PUT /users/{id}/approve - admin approves a pending user
    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approveUser(@PathVariable String id) {

        boolean done = userService.approveUser(id);
        if (done) return ApiResponse.ok("User approved", null);
        return ApiResponse.error("User not found");
    }

    // PUT /users/{id}/suspend - admin suspends a user
    @PutMapping("/{id}/suspend")
    public ApiResponse<Void> suspendUser(@PathVariable String id) {
        boolean done = userService.suspendUser(id);
        if (done) return ApiResponse.ok("User suspended", null);
        return ApiResponse.error("User not found");
    }
}
