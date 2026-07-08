package com.library.librarybackend.repository;

import com.google.cloud.firestore.*;
import com.library.librarybackend.model.BookTitle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// Handles all database reads and writes for the book_titles collection in Firestore
// Each document here is a catalog entry - one per unique title regardless of copy count
@Repository
public class BookTitleRepository {

    private static final String COLLECTION = "book_titles";

    @Autowired
    private Firestore firestore;

    // Saves a new title or overwrites an existing one
    public BookTitle save(BookTitle bt) throws InterruptedException, ExecutionException {
        if (bt.getId() == null || bt.getId().isEmpty()) {
            DocumentReference ref = firestore.collection(COLLECTION).document();
            bt.setId(ref.getId());
        }
        firestore.collection(COLLECTION).document(bt.getId()).set(toMap(bt)).get();
        return bt;
    }

    // Finds a single title by its FireStore document ID
    public Optional<BookTitle> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        return doc.exists() ? Optional.of(fromDoc(doc)) : Optional.empty();
    }

    // Returns every title in the catalog - used on the browse page
    public List<BookTitle> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Updates the four copy count fields without touching the rest of the document
    // Called by BookService every time a copy status changes
    public void updateCopyCounts(String titleId, int totalCopies, int availableCopies, int reservedCopies, int loanedCopies)
        throws InterruptedException, ExecutionException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("totalCopies", totalCopies);
        updates.put("availableCopies", availableCopies);
        updates.put("reservedCopies", reservedCopies);
        updates.put("loanedCopies", loanedCopies);
        firestore.collection(COLLECTION).document(titleId).set(updates).get();
    }

    // Adds a barcodeId to this title's copies array - called when a new copy is added
    // FieldValue.arrayUnion adds t the array without overwriting the whole thing
    public void addCopyToTitle(String titleId, String barcodeId)
        throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(titleId)
                .update("copies", FieldValue.arrayUnion(barcodeId)).get();
    }

    // Removes a barcodeId from this title's copies array - called when a copy is deleted
    public void removeCopyFromTitle(String titleId, String barcodeId)
        throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(titleId)
                .update("copies", FieldValue.arrayRemove(barcodeId)).get();
    }

    // Permanently deletes a title from the catalog
    public void delete(String id) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(id).delete().get();
    }

    // Converts a Firebase document into a BookTitle object
    // Firestore stores numbers as Long - we convert to int for the model
    @SuppressWarnings("unchecked")
    private BookTitle fromDoc(DocumentSnapshot doc) {
        BookTitle bt = new BookTitle();
        bt.setId(doc.getId());
        bt.setTitle(doc.getString("title"));
        bt.setAuthor(doc.getString("author"));
        bt.setCategory(doc.getString("category"));
        bt.setCoverImageUrl(doc.getString("coverImageUrl"));
        // Firestore returns numbers as long - convert to int with null safety
        Long total  = doc.getLong("totalCopies");
        Long avail  = doc.getLong("availableCopies");
        Long res    = doc.getLong("reservedCopies");
        Long loaned = doc.getLong("loanedCopies");
        bt.setTotalCopies    (total != null ? total.intValue() : 0);
        bt.setAvailableCopies(avail != null ? avail.intValue() : 0);
        bt.setReservedCopies (res != null ? res.intValue() : 0);
        bt.setLoanedCopies   (loaned != null ? loaned.intValue() : 0);
        // Copies is a List<String> stored as an array in Firestore
        bt.setCopies((List<String>) doc.get("copies"));
        return bt;
    }

    // Converts a BookTitle object into a Map for Firestore storage
    private Map<String, Object> toMap(BookTitle bt) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", bt.getTitle());
        m.put("author", bt.getAuthor());
        m.put("category", bt.getCategory());
        m.put("coverImageUrl", bt.getCoverImageUrl());
        m.put("totalCopies", bt.getTotalCopies());
        m.put("availableCopies", bt.getAvailableCopies());
        m.put("reservedCopies", bt.getReservedCopies());
        m.put("loanedCopies", bt.getLoanedCopies());
        // Store empty list if copies is null - avoids null pointer errors later
        m.put("copies", bt.getCopies() != null ? bt.getCopies() : new ArrayList<>());
        return m;
    }

}
