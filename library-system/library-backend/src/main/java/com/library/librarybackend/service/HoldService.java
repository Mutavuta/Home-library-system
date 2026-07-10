package com.library.librarybackend.service;

import com.library.librarybackend.model.*;
import com.library.librarybackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Manages the full hold lifecycle: placing, approving, cancelling, and expiring holds
// Also handles adding borrowers to the waitlist when no copies are available
@Service
public class HoldService {

    @Autowired private HoldRepository holdRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private BookTitleRepository bookTitleRepository;
    @Autowired private WaitlistRepository waitlistRepository;
    @Autowired private NotificationService notificationService;
    @Autowired private BookService bookService;

    // Injected from applcation.properties - how many days borrower has to collect after approval
    @Value("${app.hold.expiry.days}")
    private int holdExpiryDays;

    // Borrower places a hold request on a title
    // If copies are available a pending hold is created
    // If not the borrower is added to the waitlist and an error is thrown with their position
    public Hold placeHold(String userId, String titleId)
        throws ExecutionException, InterruptedException {
        BookTitle bookTitle = bookTitleRepository.findById(titleId)
                .orElseThrow(() -> new RuntimeException("Book title not found"));

        // Prevent a borrower from holding the same title twice
        List<Hold> existingHolds = holdRepository.findByUserId(userId);
        boolean alreadyHolding = existingHolds.stream()
                .anyMatch(h -> titleId.equals(h.getTitleId())
                && ("pending".equals(h.getStatus()) || "approved".equals(h.getStatus())));
        if (alreadyHolding) {
            throw new RuntimeException("You already have an active hold for this book.");
        }

        if (bookTitle.getAvailableCopies() > 0) {
            // Copies available - create a pending hold for admin to approve
            Hold hold = new Hold();
            hold.setUserId(userId);
            hold.setTitleId(titleId);
            hold.setTitle(bookTitle.getTitle());
            hold.setRequestDate(LocalDate.now().toString());
            hold.setStatus("pending");
            hold.setReservationLocked(false);
            return holdRepository.save(hold);
        } else {
            // No copies available - add to waitlist
            int position = waitlistRepository.countWaitingByTitleId(titleId) + 1;
            WaitlistEntry entry = new WaitlistEntry();
            entry.setUserId(userId);
            entry.setTitleId(titleId);
            entry.setRequestDate(LocalDate.now().toString());
            entry.setStatus("waiting");
            entry.setPosition(position);
            waitlistRepository.save(entry);
            throw new RuntimeException(
                    "No copies available. You have been added to the waitlist at position " + position + ".");
        }
    }

    // Admin assigns a specific copy to a pending hold and marks it as approved
    // The copy is locked as reserved so nothing else can claim it
    public Hold approveHold(String holdId, String assignedBarcodeId)
        throws ExecutionException, InterruptedException {
        Hold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new RuntimeException("Hold not found"));

        if (!"pending".equals(hold.getStatus())) {
            throw new RuntimeException("Hold is not in pending status.");
        }

        Book book = bookRepository.findById(assignedBarcodeId)
                .orElseThrow(() -> new RuntimeException("Book copy not found"));

        if (!"available".equals(book.getStatus())) {
            throw new RuntimeException("This copy is not available.");
        }

        // Reserve the copy for this borrower and approve the hold
        bookRepository.updatesStatus(assignedBarcodeId, "reserved", hold.getUserId());
        holdRepository.updateStatus(holdId, "approved", LocalDate.now().toString(), assignedBarcodeId);
        bookService.refreshTitleCounts(hold.getTitleId());

        // Notify the borrower that their book is ready to collect
        notificationService.notifyHoldApproved(hold.getUserId(), hold.getTitle(), holdExpiryDays);

        return holdRepository.findById(holdId).orElseThrow();
    }

    // Borrower cancels their own pending hold
    public void canceHold(String holdId, String userId)
        throws ExecutionException, InterruptedException {
        Hold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new RuntimeException("Hold not found"));

        // Only the owner of the hold can cancel it
        if (!hold.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorised to cancel  this hold.");
        }
        // Can only cancel before it is spproved
        if (!"pending".equals(hold.getStatus())) {
            throw new RuntimeException("Only pending holds can be cancelled.");
        }

        holdRepository.updateStatus(holdId, "abandoned", null, null);
    }

    // Expires an approved hold that the borrower did not collect in time and sets the book free
    public void expireHold(String holdId) throws ExecutionException, InterruptedException {
        Hold hold = holdRepository.findById(holdId)
                .orElseThrow(() -> new RuntimeException("Hold not found"));

        // If a copy was assigned, release it back to available
        if ("approved".equals(hold.getStatus()) && hold.getAssignedBarcodeId() != null) {
            bookRepository.updatesStatus(hold.getAssignedBarcodeId(), "available", null);
            bookService.refreshTitleCounts(hold.getTitleId());
            notifyNextOnWaitlist(hold.getTitleId(), hold.getTitle());
        }
         holdRepository.updateStatus(holdId, "abandoned", null, null);
        notificationService.notifyHoldAbandoned(hold.getUserId(), hold.getTitle());
    }

    // Checks the waitlist for a title and notifies the next person in line
    // Called after a hold expires or a book is returned
    private void notifyNextOnWaitlist(String titleId, String bookTitle)
        throws ExecutionException, InterruptedException {
        List<WaitlistEntry> waiting = waitlistRepository.findByTitleIdAndStatus(titleId, "waiting");
        if (!waiting.isEmpty()) {
            // Position ! is always at index 0 because findByTitleIdAndStatus orders by position
            WaitlistEntry next = waiting.get(0);
            waitlistRepository.updateStatus(next.getId(), "notified");
            notificationService.notifyBookAvailable(next.getUserId(), bookTitle);
        }
    }

    // Returns all holds for a specific borrower
    public List<Hold> getUserHolds(String userId) throws ExecutionException, InterruptedException {
        return holdRepository.findByUserId(userId);
    }

    // Returns every hold in the system
    public List<Hold> getAllHolds() throws ExecutionException, InterruptedException {
        return holdRepository.findAll();
    }

    // Returns holds filtered by status
    public List<Hold> getHoldByStatus(String status) throws ExecutionException, InterruptedException {
        return holdRepository.findByStatus(status);
    }
}
