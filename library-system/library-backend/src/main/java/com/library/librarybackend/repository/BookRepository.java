package com.library.librarybackend.repository;

import com.google.cloud.firestore.*;
import com.library.librarybackend.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// Handles all database reads and writes for the book collection in Firestore
// Each document in this collection is one physical copy identified by its barcodeId
@Repository
public class BookRepository {

    private static final String COLLECTION = "books";

    @Autowired
    private Firestore firestore;

    // Saves a book copy - uses barcodeId as the document ID since it is already unique
    public Book save(Book book) throws InterruptedException, ExecutionException {
        firestore.collection(COLLECTION).document(book.getBarcodeId()).set(toMap(book)).get();
        return book;
    }

    // Finds a single copy by its barcode - used when scanning a book in the Android app
    public Optional<Book> findById(String barcodeId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(barcodeId).get().get();
        return doc.exists() ? Optional.of(fromDoc(doc)) : Optional.empty();
    }

    // Returns all physical copies belonging to a specific title
    public List<Book> findByTitleId(String titleId)  throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("titleId", titleId).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns all copies with a given status - e.g. ("available")
    public List<Book> findByStatus(String status) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("status", status).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns available copies for a specific title - used when approving a hold
    public List<Book> findByTitleIdAndStatus(String titleId, String status)
            throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("titleId", titleId)
                .whereEqualTo("status", status).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Updates only status and currentHolderId - called when a copy is reserved, loaned or returned
    public void updatesStatus(String barcodeId, String status, String currentHolderId)
            throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        // currentHolderId becomes null when a book is returned - clears the previous holder
        updates.put("currentHolderId", currentHolderId);
        firestore.collection(COLLECTION).document(barcodeId).update(updates).get();
    }

    // Returns every copy in the library - used by admin for the full inventory view
    public List<Book> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Permanently deletes a copy from Firestore - used when removing a damaged book
    public void delete(String barcodeId) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(barcodeId).delete().get();
    }

    // Converts a Firestore document into a Book object
    private Book fromDoc(DocumentSnapshot doc) {
        Book b = new Book();
        // The document ID is the barcode - not stored as a field, lives in doc.getId()
        b.setBarcodeId(doc.getId());
        b.setTitleId(doc.getString("titleId"));
        b.setTitle(doc.getString("title"));
        b.setAuthor(doc.getString("author"));
        b.setStatus(doc.getString("status"));
        b.setCurrentHolderId(doc.getString("currentHolderId"));
        b.setDateAdded(doc.getString("dateAdded"));
        return b;
    }

    // Converts a Book object into a Map for Firestore storage
    private Map<String, Object> toMap(Book b) {
        Map<String, Object> m = new HashMap<>();
        m.put("barcodeId", b.getBarcodeId());
        m.put("titleId", b.getTitleId());
        m.put("title", b.getTitle());
        m.put("author", b.getAuthor());
        m.put("status", b.getStatus());
        m.put("currentHolderId", b.getCurrentHolderId());
        m.put("dateAdded", b.getDateAdded());
        return m;
    }

}
