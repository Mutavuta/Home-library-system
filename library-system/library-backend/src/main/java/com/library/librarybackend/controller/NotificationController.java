package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.Notification;
import com.library.librarybackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Handles all notification-related HTTP requests
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    // Spring injects NotificationsService automatically
    @Autowired
    private NotificationService notificationService;

    // Get /notifications/user/{userIdId} - gets all notifications for a user
    // Website calls this when borrower opens notifications page
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Notification>> getNotifications(
            @PathVariable String userId) {
        return ApiResponse.ok("notifications fetched",
                notificationService.getNotificationsForUser(userId));
    }

    // Get /notification/usert/{userId}/unread - gets unread count
    // Website calls this on every page load for the bell badge
    @GetMapping("/user/{userId}/unread")
    public ApiResponse<Long> getUnreadCount(@PathVariable String userId) {
        return ApiResponse.ok("Unread count",
                notificationService.countUnread(userId));
    }

    // PUT /notifications/user/{userId}/read - marks all as read
    // Called when borrower opens the notifications page
    @PutMapping("/user/{userId}/read")
    public ApiResponse<Void> markAllRead(@PathVariable String userId) {
        notificationService.markAllRead(userId);
        return ApiResponse.ok("All notifications marked as read", null);
    }

    // POST /notification/send - sends a notification manually
    // Useful for testing - in production notification are sent
    // automatically by services (HoldService, LoanService etc.)
    @PostMapping("/send")
    public ApiResponse<Void> sendNotification(
            @RequestBody Map<String, String> body) {
        String userId  = body.get("userId");
        String title   = body.get("title");
        String message = body.get("message");

        // Validate all fields are present
        if( userId == null || title == null || message == null ){
            return ApiResponse.error("All fields are required");
        }

        notificationService.send(userId, title, message);
        return ApiResponse.ok("Notification sent", null);
    }


}
