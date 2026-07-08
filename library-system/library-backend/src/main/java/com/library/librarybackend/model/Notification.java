package com.library.librarybackend.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

// Represents an alert sent to a borrower by the system
// Borrowers read these on the notifications page of the website
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    // Firebase document ID
    private String id;

    // Type controls which emoji the website shows:
    // "hold_approved"  = admin approved your hold
    // "hold_expiring"  = your hold expires tomorrow
    // "hold_abandoned" = your hold was cancelled
    // "due_reminder"   = book due in 2 days
    // "book_available" = waitlisted book is now available
    //private String type;

    // Who this notification is for
    private String userId;

    // Short heading shown on the notification card
    private String title;

    // Full notification message text
    private String message;

    // False when created - true after borrower reads it
    // Used for the unread count badge on the bell icon
    private boolean read;

    // When this notification was created - for sorting newest first
    private String createdAt;

}
