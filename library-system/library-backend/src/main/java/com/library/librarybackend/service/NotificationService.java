package com.library.librarybackend.service;

import com.library.librarybackend.model.Notification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    // In-memory notification storage - replaced with Firebase later
    private List<Notification> notifications = new ArrayList<>();

    // Returns all notifications for one user newest first
    public List<Notification> getNotificationsForUser(String userId) {
        return notifications.stream()
                .filter(n -> n.getUserId().equals(userId))
                // Sort by createdAt descending
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // Counts unread notifications for a user
    // Used for thr bell icon badge count
    public long countUnread(String userId) {
        return notifications.stream()
                .filter(n -> n.getUserId().equals(userId) && !n.isRead())
                .count();
    }

    // Marks all notifications as read for user
    // Called when borrower opens the notification page
    public void markAllRead(String userId) {
        notifications.stream()
                .filter(n -> n.getUserId().equals(userId))
                .forEach(n -> n.setRead(true));
    }

    // Core method - creates and saves a notification
    // All helper methods below call this ona
    public void send(String userId, String title, String message) {
        Notification n = new Notification();
        // Generate a simple ID using timestamp
        n.setId(String.valueOf(System.currentTimeMillis()));
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        // New notifications start as unread
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now().toString());
        notifications.add(n);
    }

    // Tells borrower their hold was approved
    public void notifyHoldApproved(String userId,
                                   String bookTitle, int daysToCollect) {
        send(userId,
                "Hold Approved",
                "Your hold for '" + bookTitle + "' is approved. " +
                "You have " + daysToCollect + " days to collect it. ");
    }

    // Warns borrower that their hold expires tomorrow
    public void notifyHoldExpiring(String userId, String bookTitle) {
        send(userId,
                "Hold Expiring Tomorrow",
                "Your hold for '" + bookTitle + "' expires tomorrow. " +
                "Please collect it today.");
    }

    // Tells borrower their hold was cancelled or expired
    public void notifyHoldAbandoned(String userId, String bookTitle) {
        send(userId,
                "Hold Expired",
                "Your hold for '" + bookTitle + "' has expired " +
                "and the copy has been released. ");
    }

    // Reminds borrower their book is due soon
    public void notifyDueReminder(String userId, String bookTitle) {
        send(userId,
                "Return Reminder",
                "'" + bookTitle + "' is due in 2 days. " +
                "Please return it soon. ");
    }

    // Tells waitlisted borrower a copy is now available
    public void notifyBookAvailable(String userId, String bookTitle) {
        send(userId,
                "Book Now Available",
                "A copy of '" + bookTitle + "' is now available. " +
                "Secure a copy as soon as possible.");
    }

}
