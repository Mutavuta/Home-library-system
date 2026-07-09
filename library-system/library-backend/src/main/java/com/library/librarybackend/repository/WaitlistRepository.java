package com.library.librarybackend.repository;

import com.google.cloud.firestore.*;
import com.library.librarybackend.model.WaitlistEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// Handles all database reads and writes for the waitlist collection in Firestore
@Repository
public class WaitlistRepository {

    private static final String COLLECTION = "waitlist";

    @Autowired
    private Firestore firestore;

    // Saves a new waitlist entry - Firestore generates the document ID
    public WaitlistEntry save(WaitlistEntry entry) throws ExecutionException, InterruptedException {
        if (entry.getId() == null || entry.getId().isEmpty()) {
            DocumentReference ref = firestore.collection(COLLECTION).document();
            entry.setId(ref.getId());
        }
        firestore.collection(COLLECTION).document(entry.getId()).set(toMap(entry)).get();
        return entry;
    }

    // Returns all waiting entries for a title sorted by position
    // Position 1 is always first - gets notified when a copy becomes available
    public List<WaitlistEntry> findByTitleIdAndStatus(String titleId, String status)
            throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("titleId", titleId)
                .whereEqualTo("status", status)
                .orderBy("position").get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns all waitlist entries for a user - shows what they are queued for
    public List<WaitlistEntry> findByUserId(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns how many people are currently waiting for a title
    // Used to calculate the position for a new entry: count + 1
    public int countWaitingByTitleId(String titleId) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("titleId", titleId)
                .whereEqualTo("status", "waiting").get().get();
        return qs.size();
    }

    // Updates only the status field on a waitlist entry
    // e.g. "waiting" -> "notified" when a copy becomes available
    public void updateStatus(String id, String status) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).update("status", status).get();
    }

    // Converts a Firestore document into a WaitlistEntry object
    private WaitlistEntry fromDoc(DocumentSnapshot doc) {
        WaitlistEntry e = new WaitlistEntry();
        e.setId(doc.getId());
        e.setUserId(doc.getString("userId"));
        e.setTitleId(doc.getString("titleId"));
        e.setRequestDate(doc.getString("requestDate"));
        e.setStatus(doc.getString("status"));
        // getLong returns null if missing - default to 0
        Long pos = doc.getLong("position");
        e.setPosition(pos != null ? pos.intValue() : 0);
        return e;
    }

    // Converts a WaitlistEntry object into a Map for Firestore storage
    private Map<String, Object> toMap(WaitlistEntry e) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", e.getUserId());
        m.put("titleId", e.getTitleId());
        m.put("requestDate", e.getRequestDate());
        m.put("status", e.getStatus());
        m.put("position", e.getPosition());
        return m;
    }
}