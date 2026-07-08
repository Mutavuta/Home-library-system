package com.library.librarybackend.service;

import com.library.librarybackend.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    // In-memory user storage - replaced with Firebase later
    private List<User> users = new ArrayList<>();

    // Returns all users
    public List<User> getAllUsers() {
        return users;
    }

    // Creates a new user and sets role to borrower and status to pending
    public User createUser(User user) {
        user.setId(UUID.randomUUID().toString());
        user.setRole("Borrower");
        user.setStatus("pending");
        user.setCreatedAt(LocalDate.now().toString());
        users.add(user);
        return user;
    }

    // Finds a user by email as used during Login as user email is used s the username
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    // Finds a user by their ID
    public Optional<User> findById(String id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    // Admin approves a pending user
    // .ifPresent() runs only if the user was found
    public boolean approveUser(String id) {
        Optional<User> user = findById(id);
        user.ifPresent(u -> u.setStatus("Approved"));
        return user.isPresent();
    }

    // Admin suspends a user - blocks their access
    public boolean suspendUser(String id) {
        Optional<User> user = findById(id);
        user.ifPresent(u -> u.setStatus("Suspended"));
        return user.isPresent();
    }

    // Returns users filtered by status
    public List<User> getUsersByStatus(String status) {
        return users.stream()
                .filter(u -> u.getStatus().equals(status))
                .toList();
    }

    // Saves a user directly - used internally
    // Different from createUser() which forces role and status
    // This one trusts the values already set on the user object
    public User save(User user) {
        users.add(user);
        return user;
    }

}
