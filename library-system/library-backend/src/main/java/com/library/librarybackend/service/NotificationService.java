package com.library.librarybackend.service;

import com.library.librarybackend.model.Notification;
import com.library.librarybackend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Creates and retrieves notifications for borrowers
// Other services call the notify methods here instead of building notifications themselves
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    // Core method that builds and saves a notification
    // All the notify* methods below call this one
    private void send(String userId, String title, String message)
    throws ExecutionException, InterruptedException{
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        // Starts as unread 
        n.setRead(false);
        n.setCreatedAt(LocalDate.now().toString());
        notificationRepository.save(n);
    }

    // Returns all notifications for a user sorted newest first
    public List<Notification> getUserNoticications(String userId)
    throws ExecutionException, InterruptedException {
        return notificationRepository.findByUserId(userId);
    }

    // Returns the count of unread notifications
    public long getUnreadCount(String userId) throws ExecutionException, InterruptedException {
        return notificationRepository.countUnread(userId);
    }

    // Marks all notifications as read for a user
    public void markAllRead(String userId) throws ExecutionException, InterruptedException {
        notificationRepository.markAllRead(userId);
    }

    // Sent when admin approves a hold 
    public void notifyHoldApproved(String userId, String bookTitle, int daysToCollect)
    throws ExecutionException, InterruptedException {
        send(userId, 
            "Book Reserved for You!",
            "\"" + bookTitle + "\" is ready. Please collect within " + daysToCollect + " days."
        );
    }

    //Sent by the scheduler the day before a hold expires
    public void notifyHoldExpiring(String userId, String bookTitle)
    throws ExecutionException, InterruptedException {
        send(userId, 
            "Reservation Expiring Soon",
            "Your hold on \"" +bookTitle + "\" expires tomorrow. Please collect  it."
        );
    }

    // Sent when a hold is cancelled due to expiry
    public void notifyHoldAbandoned(String userId, String bookTitle) 
    throws ExecutionException, InterruptedException {
        send(userId,
            "Reservation Cancelled",
            "Your hold on \"" + bookTitle + "\" was cancelled due to non-collection."
        );
    }

    // Sent by the scheduler when a loan iss due in 2 days 
    public void notifyDueReminder(String userId, String bookTitle, String dueDate)
    throws ExecutionException, InterruptedException {
        send(userId, 
            "Book Due Soon",
            "\"" + bookTitle + "\" is due on " + dueDate + ". Please return it on time."
        );
    }

    // Sent to the next persion on the waitlist when a copy becomes available
    public void notifyBookAvailable(String userId, String bookTitle)
    throws ExecutionException, InterruptedException {
        send(userId, 
            "Book Now Available",
            "\"" + bookTitle + "\" is now available. Place a hold to reserve it. "
        );
    }

}
