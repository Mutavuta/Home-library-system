package com.library.librarybackend.service;

import com.library.librarybackend.model.Hold;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HoldService {

    // In-memory hold storage - replaced with Firebase later (for testing purpose)
    private List<Hold> holds = new ArrayList<>();

    // returns all holds
    public List<Hold> getAllHolds() {
        return holds;
    }

    // Find hold by ID
    public Optional<Hold> findById(String holdId) {
        return holds.stream()
                .filter(h -> h.getId().equals(holdId))
                .findFirst();
    }

    // Returns holds for one specific user
    // Used for "My Holds" page on the website
    public List<Hold> getHoldsByUserId(String userId) {
        return holds.stream()
                .filter(h -> h.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    // Return holds filtered by status
    public List<Hold> getHoldsByStatus(String status) {
        return holds.stream()
                .filter(h -> h.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    // Borrower places a hold on a book title
    public Hold placeHold(String userId, String titleId, String title) {
        Hold hold = new Hold();
        hold.setId(UUID.randomUUID().toString());
        hold.setUserId(userId);
        hold.setTitleId(titleId);
        hold.setTitle(title);
        hold.setRequestDate(LocalDate.now().toString());
        // All new holds are defaulted to pending
        hold.setStatus("pending");
        hold.setAssignedBookId(null);
        holds.add(hold);
        return hold;

    }

    // Admin approves a hold and assigns a specific copy
    // bookId = the barcode of the specific copy being assigned
    public Optional<Hold> approveHold(String holdId, String bookId) {
        Optional<Hold> holdOpt = findById(holdId);
        holdOpt.ifPresent(hold ->{
            hold.setStatus("approved");
            hold.setApprovedDate(LocalDate.now().toString());
            hold.setAssignedBookId(bookId);

        });
        return holdOpt;
    }

    // Marks a hold as collected - called when loan is created
    public Optional<Hold> markCollected(String holdId) {
        Optional<Hold> holdOpt = findById(holdId);
        holdOpt.ifPresent(hold ->hold.setStatus("Collected"));
        return holdOpt;
    }

    // Cancels or expires a hold - frees the assigned copy
    public Optional<Hold> abandonHold(String holdId) {
        Optional<Hold> holdOpt = findById(holdId);
        holdOpt.ifPresent(hold -> hold.setStatus("abandoned"));
        return holdOpt;
    }


}
