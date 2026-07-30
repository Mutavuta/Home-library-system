package com.library.librarybackend.repository;

import com.google.cloud.firestore.*;
import com.library.librarybackend.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// Handles all database reads and writes for the notifications collection
@Repository
public class NotificationRepository {

    private static final String COLLECTION = "notifications";

    @Autowired
    private Firestore firestore;

    // Saves a new notification - Firestore generates the document ID
    public Notification save(Notification n) throws ExecutionException, InterruptedException {
        if (n.getId() == null || n.getId().isEmpty()) {
            DocumentReference ref = firestore.collection(COLLECTION).document();
            n.setId(ref.getId());
        }
        firestore.collection(COLLECTION).document(n.getId()).set(toMap(n)).get();
        return n;
    }

    // Returns all notifications for a user sorted newest first
    // Used to populate the notification page on the website
    public List<Notification> findByUserId(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns the count of unread notifications
    public long countUnread(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false).get().get();
        return qs.size();
    }

    // Marks all unread notifications as read for a user
    // Uses a Firestore WriteBatch to update all documents in a single round trip
    public void markAllRead(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false).get().get();
        // A batch lets you send multiple updates to Firestore in one network call
        WriteBatch batch = firestore.batch();
        for (QueryDocumentSnapshot doc : qs.getDocuments()) {
            batch.update(doc.getReference(), "read", true);
        }
        batch.commit().get();
    }

    // Converts a Firestore document into a Notification object
    private Notification fromDoc(DocumentSnapshot doc) {
        Notification n = new Notification();
        n.setId(doc.getId());
        n.setUserId(doc.getString("userId"));
        n.setTitle(doc.getString("title"));
        n.setMessage(doc.getString("message"));
        // getBoolean can return null if the field is missing - default to false
        Boolean read = doc.getBoolean("read");
        n.setRead(read != null && read);
        n.setCreatedAt(doc.getString("createdAt"));
        return n;
    }

    // Converts a Notification object into a Map for Firestore storage
    private Map<String, Object> toMap(Notification n) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", n.getUserId());
        m.put("title", n.getTitle());
        m.put("message", n.getMessage());
        m.put("read", n.isRead());
        m.put("createdAt", n.getCreatedAt());
        return m;
    }

}
