package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.Notification;
import com.library.librarybackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Handles all notification-related HTTP requests
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired private NotificationService notificationService;

    // GET /notifications - returns all notifications for the logged-in borrower
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(Authentication auth) {
        try {
            String userId = (String) auth.getPrincipal();
            return ResponseEntity.ok(ApiResponse.ok("OK",
                    notificationService.getUserNotifications(userId)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /notfications/unread-count - returns the no. of unread notifications
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(Authentication auth) {
        try{
            String userId = (String) auth.getPrincipal();
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("count", count)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // PST /notofications/mark-read - marks all notifications as reda for a user
    @PostMapping("/mark-read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(Authentication auth) {
        try {
           String userId = (String) auth.getPrincipal();
           notificationService.markAllRead(userId);
           return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read.",null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

}
