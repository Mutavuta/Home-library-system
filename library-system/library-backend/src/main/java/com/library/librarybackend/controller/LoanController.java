package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.Loan;
import com.library.librarybackend.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

// Handles all Loan-related HTTP requests
@RestController
@RequestMapping("/loans")
public class LoanController {

    // Spring injects LoanSercive automatically
    @Autowired
    private LoanService loanService;

    // GET /loans - returns all loans
    @GetMapping
    public ApiResponse<List<Loan>> getAllLoans() {
        return ApiResponse.ok("Loans fetched" , loanService.getAllLoans());
    }

    // GET /loans/active - returns currently active loans
    // Admin uses this to see all books currently out on active loans page
    @GetMapping("/active")
    public ApiResponse<List<Loan>> getActiveLoans() {
        return ApiResponse.ok("Active Loans fetched", loanService.getActiveLoans());
    }

    // GET /loans/averdue - returns overdue loans
    // Provides the admin with the red flag list
    @GetMapping("overdue")
    public ApiResponse<List<Loan>> getOverdueLoans() throws ExecutionException, InterruptedException {
        return ApiResponse.ok("Overdue loans", loanService.getOverdueLoans());
    }

    // GET /loans/user/{userId} - returns loans for one borrower
    // Used in the website in My Loans page
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Loan>> getLoansByUser(@PathVariable String userId) {
        return ApiResponse.ok("User loans", loanService.getLoansByUserId(userId));
    }

    // POST /loans - admin confirms a loan when borrower collects the book
    // Body must contain: userId, bookId, titleId, holdId, dueDate
    @PostMapping
    public ApiResponse<Loan> createLoan(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String bookId = body.get("bookId");
        String titleId = body.get("titleId");
        String holdId = body.get("holdId");
        String dueDate = body.get("dueDate");

        // Validate all required fields
        if (userId == null || bookId == null || titleId == null || dueDate == null) {
            return ApiResponse.error("All fileds are required");
        }

        Loan loan = loanService.createLoan(
                userId, bookId, titleId, holdId, dueDate);
        return ApiResponse.ok("Loan created", loan);
    }

    // PUT /loans/return - admin processes a book return
    // Body contains: bookId (scanned barcode of returned book)
    @PutMapping("/return")
    public ApiResponse<Loan> processReturn(@PathVariable Map<String, String> body) {
        String bookId = body.get("bookId");
        // Validate bookId
        if (bookId == null) {
            return ApiResponse.error("Book Id is required");
        }

        return loanService.processReturn(bookId)
                .map(loan -> ApiResponse.ok("Book returned", loan))
                .orElse(ApiResponse.error("No active loan found for this book"));
    }

}
