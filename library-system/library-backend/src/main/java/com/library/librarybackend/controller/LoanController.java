package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.dto.ApproveLoanRequest;
import com.library.librarybackend.model.Loan;
import com.library.librarybackend.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Handles all Loan-related HTTP requests
@RestController
@RequestMapping("/loans")
public class LoanController {

    @Autowired private LoanService loanService;

    // POST /loans/admin/approbve - admin confirms  the loan
    @PostMapping("/admin/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Loan>> approveLoan(@RequestBody ApproveLoanRequest req) {
        try {
            Loan loan = loanService.approveLoan(req.getHoldId(), req.getBarcodeId(), req.getDueDate());
            return ResponseEntity.ok(ApiResponse.ok("Loan confirmed successfully", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /loans/admin/return - admin scans a returned book to process the return
    @PostMapping("/admin/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Loan>> returnBook(@RequestBody Map<String, String> body) {
        try {
            Loan loan = loanService.processReturn(body.get("barcodeId"));
            return ResponseEntity.ok(ApiResponse.ok("Book returned successfully", loan));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /loans/admin/active - returns all currently active loans
    @GetMapping("admin/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Loan>>> activeLoans() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("OK", loanService.getAllActiveLoans()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /loans/admin/overdue - returns overdue loans
    @GetMapping("/admin/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Loan>>> overdueLoans() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("OK", loanService.getOverdueLoans()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /loans/admin/all -returns full loan history
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Loan>>> allLoans() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("OK", loanService.getAllLoans()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /loans/mine -returns all loans for the logged-in borrower
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<Loan>>> myLoans(Authentication auth) {
        try {
            String userId = (String) auth.getPrincipal();
            return ResponseEntity.ok(ApiResponse.ok("OK", loanService.getUserLoans(userId)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }
}
