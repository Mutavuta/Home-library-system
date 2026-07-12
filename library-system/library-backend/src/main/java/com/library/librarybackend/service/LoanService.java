package com.library.librarybackend.service;

import com.library.librarybackend.model.*;
import com.library.librarybackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages the loan lifecycle: approving loans when borrower collects and processing returns
@Service
public class LoanService {

    @Autowired private LoanRepository loanRepository;
    @Autowired private HoldRepository holdRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private WaitlistRepository waitlistRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private BookService bookService;

    // Admin confirms the loan when the borrower physically collects the book
    // Admin scans the barcode - system verifies it matches the approved hold assignment
    public Loan approveLoan(String holdId, String scannedBarcodeId, String dueDate)
        throws ExecutionException, InterruptedException {
        Hold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new RuntimeException("Hold not found"));
        // Hold must be pre-approved before a loan can be confirmed
        if (!"approved".equals(hold.getStatus())) {
            throw new RuntimeException("Hold must be approved pre-approved confirming loan.");
        }

        // Verify the scanned barcode matches what was assigned to this hold
        if (!scannedBarcodeId.equals(hold.getAssignedBarcodeId())) {
            Book scanned = bookRepository.findById(scannedBarcodeId)
                    .orElseThrow(() -> new RuntimeException("Scanned book not found"));
            // If it is a different copy of the same title, admin override is allowed
            if (!scanned.getTitleId().equals(hold.getTitleId())) {
                throw new RuntimeException(
                        "MISMATCH: Scanned book \"" + scanned.getTitle()
                        + "\" does not match hold for \"" + hold.getTitle() + "\" . Please check."
                );
            }
        }

        // Create the loa record
        Loan loan = new Loan();
        loan.setUserId(hold.getUserId());
        loan.setBarcodeId(scannedBarcodeId);
        loan.setTitleId(hold.getTitleId());
        loan.setHoldId(holdId);
        loan.setLoanDate(LocalDate.now().toString());
        loan.setDueDate(dueDate);
        loan.setStatus("active");
        loanRepository.save(loan);

        // Mark the copy as loaned and the hold as collected and the hold as collected
        bookRepository.updatesStatus(scannedBarcodeId, "loaned", hold.getUserId());
        holdRepository.updateStatus(holdId, "collected", null, null);

        // Recalculate title copy counts now that one copy changed to loaned
        bookService.refreshTitleCounts(hold.getTitleId());

        return loan;
    }

    // Admin scans returned book barcode to process a return
    // Finds the active loan, marks it returned, frees the copy, notifies the waitlist
    public Loan processReturn(String barcodeId) throws ExecutionException, InterruptedException {
        // Find the active loan for this copy
        Loan loan = loanRepository.findActiveByBarcodeId(barcodeId)
                .orElseThrow(() -> new RuntimeException("No active loan found for barcode: " + barcodeId));

        String today = LocalDate.now().toString();
        loanRepository.markReturned(loan.getId(), today);

        // Free the copy back to available
        bookRepository.updatesStatus(barcodeId, "available", null);
        bookService.refreshTitleCounts(loan.getTitleId());

        // Check if anyone is waiting for this title and notify them
        List<WaitlistEntry> waiting = waitlistRepository
                .findByTitleIdAndStatus(loan.getTitleId(), "waiting");
        if (!waiting.isEmpty()) {
            WaitlistEntry next = waiting.get(0);
            waitlistRepository.updateStatus(next.getId(), "notified");
            Book book = bookRepository.findById(barcodeId).orElse(null);
            // Use title from book if available, fallback to generic message
            String title = book != null ? book.getTitle() : "A book";
            notificationService.notifyBookAvailable(next.getUserId(), title);
        }

        // Update and return the loan object with final status
        loan.setStatus("returned");
        loan.setReturnDate(today);
        return loan;
    }

    // Returns all currently active loans
    public List<Loan> getAllActiveLoans() throws ExecutionException, InterruptedException {
        return loanRepository.findAllActive();
    }

    // Returns all loans for a specific borrower
    public List<Loan> getUserLoans(String userId) throws ExecutionException, InterruptedException {
        return loanRepository.findByUserId(userId);
    }

    // Returns every loan ever created
    public List<Loan> getAllLoans() throws ExecutionException, InterruptedException {
        return loanRepository.findAll();
    }

    // Returns active loans where the due date is in the past - used byn the scheduler
    public List<Loan> getOverdueLoans() throws ExecutionException, InterruptedException {
        String today = LocalDate.now().toString();
        List<Loan> active = loanRepository.findAllActive();
        // String date comparison works here because format is yyyy-mm-dd - sorts lexicographically
        return active.stream()
                .filter(l -> l.getDueDate() != null && l.getDueDate().compareTo(today) < 0)
                .toList();
    }

}
