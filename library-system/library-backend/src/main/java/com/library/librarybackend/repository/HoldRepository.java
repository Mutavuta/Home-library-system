package com.library.librarybackend.repository;

import com.google.cloud.firestore.*;
import com.library.librarybackend.model.Hold;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// Handles all database reads and writes for the holds collection in Firestore
@Repository
public class HoldRepository {

    private static final String COLLECTION = "holds";

    @Autowired
    private Firestore firestore;

    // Saves a new hold or overwrites an existing one
    public Hold save(Hold hold) throws ExecutionException, InterruptedException {
        if (hold.getId()  == null || hold.getId().isEmpty()) {
            DocumentReference ref  = firestore.collection(COLLECTION).document();
            hold.setId(ref.getId());
        }
        firestore.collection(COLLECTION).document(hold.getId()).set(toMap(hold)).get();
        return hold;
    }

    // Finds a single hold by its Firestore document ID
    public Optional<Hold> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        return doc.exists() ? Optional.of(fromDoc(doc)) : Optional.empty();
    }

    // Returns  all holds placed by a specific user
    public List<Hold> findByUserId(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns all holds with a given status
    public List<Hold> findByStatus(String status) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("status", status).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns holds for a user filtered by status
    public List<Hold> findByUserIdAndStatus(String userId, String status)
            throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", status).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns every hold in the system - used by admin  to see all requests
    public List<Hold> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Updates hold status and optionally sets approvedDate and assignedBarcodeId
    // Only updates fields that are not null - avoids overwriting data we want to keep
    public void updateStatus(String holdId, String status, String approvedDate, String assignedBarcodeId)
            throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        if (approvedDate != null) updates.put("approvedDate", approvedDate);
        if (assignedBarcodeId != null) {
            updates.put("assignedBarcodeId", assignedBarcodeId);
            // Lock the reservation so no other process can assign the same copy
            updates.put("reservationLocked", true);
        }
        firestore.collection(COLLECTION).document(holdId).update(updates).get();
    }

    // Converts a Firestore document into a Hold object
    private Hold fromDoc(DocumentSnapshot doc) {
        Hold h = new Hold();
        h.setId(doc.getId());
        h.setUserId(doc.getString("userId"));
        h.setTitleId(doc.getString("titleId"));
        h.setTitle(doc.getString("title"));
        h.setRequestDate(doc.getString("requestDate"));
        h.setApprovedDate(doc.getString("approvedDate"));
        h.setStatus(doc.getString("status"));
        h.setAssignedBarcodeId(doc.getString("assignedBarcodeId"));
        // getBoolean can return null if the field doesn't exist - default to false
        Boolean locked = doc.getBoolean("reservationLocked");
        h.setReservationLocked(locked != null && locked);
        return h;
    }

    // Converts a Hold object into a Map for Firestore storage
    private Map<String, Object> toMap(Hold h) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", h.getUserId());
        m.put("titleId", h.getTitleId());
        m.put("title", h.getTitle());
        m.put("requestDate", h.getRequestDate());
        m.put("approvedDate", h.getApprovedDate());
        m.put("status", h.getStatus());
        m.put("assignedBarcodeId", h.getAssignedBarcodeId());
        m.put("reservationLocked", h.isReservationLocked());
        return m;
    }

}
