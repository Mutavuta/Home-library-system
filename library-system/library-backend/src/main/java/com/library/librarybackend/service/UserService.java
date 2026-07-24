package com.library.librarybackend.service;

import com.library.librarybackend.model.User;
import com.library.librarybackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;

    // Returns all accounts waiting foe admin approval - pending users list on app
    public List<User> getPendingUsers() throws ExecutionException, InterruptedException {
        return userRepository.findByStatus("pending");
    }

    // Returns every user in the system - admin app in all users
    public List<User> getAllUsers() throws ExecutionException, InterruptedException {
        return userRepository.findAll();
    }

    // Finds a single user by their id
    public Optional<User> getUserById(String id) throws ExecutionException, InterruptedException {
        return userRepository.findById(id);
    }

    // Approves a pending borrower account
    public User approveUser(String userId) throws ExecutionException, InterruptedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        userRepository.updateStatus(userId, "approved");
        user.setStatus("approved");
        return user;
    }

    // Suspends an account
    public User suspendUser(String userId) throws ExecutionException, InterruptedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.updateStatus(userId, "suspended");
        user.setStatus("suspended");
        return user;
    }

    // Re-activates a suspended account
    public User reactivateUser(String userId) throws ExecutionException, InterruptedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.updateStatus(userId, "approved");
        user.setStatus("approved");
        return user;
    }

    // Updates a borrower's own name and phone
    public void updateProfile(String userId, String fullName, String phone)
        throws ExecutionException, InterruptedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(fullName);
        user.setPhone(phone);
        userRepository.save(user);
    }

}
