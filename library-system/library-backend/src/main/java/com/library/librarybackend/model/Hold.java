package com.library.librarybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Represents a borrower's request to borrow a book
// A hold must be approved by admin before becoming a loan
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hold {

    // Firebase document ID
    private String id;
    // Who placed the hold
    private String userId;
    // Which book title was requested
    private String titleId;
    // Book title stored here to avoid extra lookups when displaying
    private String title;
    // When the hold was placed - yyyy-mm-dd
    private String requestDate;
    // When admin approved it - null until approved
    private String approvedDate;

    // Hold lifecycle:
    // pending = waiting for approval
    // approved = admin approved but the user has not yet collected
    // collected = borrower collected, loan created
    // abandoned = expired(user did not collect) or cancelled
    private String status;

    // Which specific copy was assigned to this hold - null until admin approves and assigns a copy
    private String assignedBarcodeId;

    // True once a copy has been physically locked/reserved for this hold -
    // a safety flag so two different processes can't both try to assign a copy
    private boolean reservationLocked;
}
