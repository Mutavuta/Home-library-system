package com.library.librarybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Represents a confirmed loan - book is physically with the borrower
// Created when borrower collects the book from the library
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    // Firebase document ID
    private String id;
    // Who borrowed the book
    private String userId;
    // Which physical copy is out - the barcode ID
    private String barcodeId;
    // Which title - for display purposes
    private String titleId;
    // The hold this loan came from - audit trail
    // You can always trace: hold > loan > borrower
    private String holdId;
    // When borrower collected the book
    private String loanDate;
    // When the book must be returned
    // Set by admin at loan confirmation time
    private String dueDate;
    // When borrower actually returned it - null until returned
    private String returnDate;
    // Loans status - active(book is out),returned,overdue
    private String status;

}
