package com.library.librarybackend.dto;

import lombok.Data;

// Request body shape for POST /api/loans/approve
// Admin sends this when confirming that a borrower has physically collected a book
@Data
public class ApproveLoanRequest {

    // The hold that is being converted into a loan
    private String holdId;

    // Which physical copy the borrower is taking - must match the barcode
    // already assigned to this hold
    private String barcodeId;

    // When the borrower should return the book - yyyy-mm-dd
    // Admin sets this manually, usually today + app.loan.default.days
    private String dueDate;

}
