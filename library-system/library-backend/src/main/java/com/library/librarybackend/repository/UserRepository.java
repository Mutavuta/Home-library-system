package com.library.librarybackend.repository;

import com.google.cloud.firestore.*;
import com.library.librarybackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// Handles all database reads and writes for the users collection in Firestore
@Repository
public class UserRepository {

    // The Firestore collection name - matches what you see in the Firebase console
    private static final String COLLECTION = "users";

    // Injected by FirebaseConfig - the connection to Firestore
    @Autowired
    private Firestore firestore;

    // Saves a new user or overwrites an existing one
    // If the user has no ID yet, Firestore generates one, and we store it back on the object
    public User save(User user) throws ExecutionException, InterruptedException {
        if (user.getId() == null || user.getId().isEmpty()) {
            // Let firestore generate a unique document ID
            DocumentReference docRef = firestore.collection(COLLECTION).document();
            user.setId(docRef.getId());
        }

        // .get() at the end blocks until Firestore confirms the write is done
        firestore.collection(COLLECTION).document(user.getId()).set(toMap(user)).get();
        return user;
    }

    // Finds a single user by their Firestore document ID
    public Optional<User> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        // Optional.empty() if the document does not exist - caller checks this
        return doc.exists() ? Optional.of(fromDoc(doc)) : Optional.empty();
    }

    // Finds a user by email - used at login to check if the email is registered
    public Optional<User> findByEmail(String email) throws ExecutionException, InterruptedException {
        QuerySnapshot query = firestore.collection(COLLECTION)
                .whereEqualTo("email", email)
                // limit(1) - we only need one result, stops Firestore reading the whole collection
                .limit(1).get().get();
        if (query.isEmpty()) return Optional.empty();
        return Optional.of(fromDoc(query.getDocuments().get(0)));
    }

    // Returns all users with a given status - e.g findByStatus("pending") for admin approval list
    public List<User> findByStatus(String status) throws ExecutionException, InterruptedException {
        QuerySnapshot query = firestore.collection(COLLECTION)
                .whereEqualTo("status", status).get().get();
        // Convert each Firestore document into a User object using fromDoc()
        return query.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns every user in the collection - used by admin to manage all accounts (all users screen)
    public List<User> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot query = firestore.collection(COLLECTION).get().get();
        return query.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Updates only the status field on an existing user document - avoids overwriting other fields
    public void updateStatus(String userId, String status) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(userId)
                .update("status", status).get();
    }

    // Permanently deletes a user document from Firestore
    public void delete(String userId) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(userId).delete().get();
    }

    // Converts a user object into a plain Map so Firestore can store it
    // Every field must be listed here - Firestore stores exactly what is in this map
    private Map<String, Object> toMap(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("role", u.getRole());
        m.put("status", u.getStatus());
        m.put("passwordHash", u.getPasswordHash());
        m.put("createdAt", u.getCreatedAt());
        return m;
    }

    // Converts a Firestore document snapshot back into a User object
    // doc.getId() give the document ID which is used as the user's ID
    private User fromDoc(DocumentSnapshot doc) {
        User u = new User();
        u.setId(doc.getId());
        u.setFullName(doc.getString("fullName"));
        u.setEmail(doc.getString("email"));
        u.setPhone(doc.getString("phone"));
        u.setRole(doc.getString("role"));
        u.setStatus(doc.getString("status"));
        u.setPasswordHash(doc.getString("passwordHash"));
        u.setCreatedAt(doc.getString("createdAt"));
        return u;
    }

}
