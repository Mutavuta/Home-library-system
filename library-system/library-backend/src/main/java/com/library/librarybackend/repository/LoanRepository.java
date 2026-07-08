package com.library.librarybackend.repository;

import com.google.cloud.firestore.*;
import com.library.librarybackend.model.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

// Handles all database reads and writes for the loans collection in Firestore
@Repository
public class LoanRepository {

    private static final String COLLECTION = "loans";

    @Autowired
    private Firestore firestore;

    // Saves a new loan or overwrites an existing one
    public Loan save(Loan loan) throws ExecutionException,  InterruptedException {
        if (loan.getId() == null || loan.getId().isEmpty()) {
            DocumentReference ref = firestore.collection(COLLECTION).document();
            loan.setId(ref.getId());
        }
        firestore.collection(COLLECTION).document(loan.getId()).set(toMap(loan)).get();
        return loan;
    }

    // Finds a single loan by its Firestore document ID
    public Optional<Loan> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION).document(id).get().get();
        return doc.exists() ? Optional.of(fromDoc(doc)) : Optional.empty();
    }

    // Returns all loans for a user
    public List<Loan> findByUserId(String userId) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns Loans filtered by status
    public List<Loan> findByStatus(String status) throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("status", status).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns all currently active loans - used by the scheduler to check for overdue books
    public List<Loan> findAllActive() throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("status", "active").get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Returns every loan ever created - used by admin for full history view
    public List<Loan> findAll() throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION).get().get();
        return qs.getDocuments().stream().map(this::fromDoc).collect(Collectors.toList());
    }

    // Finds the active loan for a specific copy - used when processing a book return
    public Optional<Loan> findActiveByBarcodeId(String barcodeId)
        throws ExecutionException, InterruptedException {
        QuerySnapshot qs = firestore.collection(COLLECTION)
                .whereEqualTo("barcodeId", barcodeId)
                .whereEqualTo("status", "active")
                .limit(1).get().get();
        if (qs.isEmpty()) return Optional.empty();
        return Optional.of(fromDoc(qs.getDocuments().get(0)));
    }

    // Marks a loan as returned and records the return date
    // Called by LoanService when the librarian scans a book back in
    public void markReturned(String loanId, String returnDate)
            throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "returned");
        updates.put("returnDate", returnDate);
        firestore.collection(COLLECTION).document(loanId).update(updates).get();
    }

    // Updates only the status field - used to mark loans as overdue
    public void updatesStatus(String loanId, String status)
        throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(loanId).update("status", status).get();
    }

    // Converts a Firebase document into a Loan object
    private Loan fromDoc(DocumentSnapshot doc) {
        Loan l = new Loan();
        l.setId(doc.getId());
        l.setUserId(doc.getString("userId"));
        l.setBarcodeId(doc.getString("barcodeId"));
        l.setTitleId(doc.getString("titleId"));
        l.setHoldId(doc.getString("holdId"));
        l.setLoanDate(doc.getString("loanDate"));
        l.setDueDate(doc.getString("dueDate"));
        l.setReturnDate(doc.getString("returnDate"));
        l.setStatus(doc.getString("status"));
        return l;
    }

    // Converts a loan object into a Map for firestore storage
    private Map<String, Object> toMap(Loan l) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", l.getUserId());
        m.put("barcodeId", l.getBarcodeId());
        m.put("titleId", l.getTitleId());
        m.put("holdId", l.getHoldId());
        m.put("loanDate", l.getLoanDate());
        m.put("dueDate", l.getDueDate());
        m.put("returnDate", l.getReturnDate());
        m.put("status", l.getStatus());
        return m;
    }

}
