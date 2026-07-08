package com.library.librarybackend.service;

import com.library.librarybackend.model.Loan;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoanService {

    // In-memory loan storage - replaced with Firebase later
    private List<Loan> loans = new ArrayList<>();

    // Returns all loans
    public List<Loan> getAllLoans() {
        return loans;
    }

    // Returns only active loans
    public List<Loan> getActiveLoans() {
        return loans.stream()
                .filter(l -> l.getId().equals("active"))
                .collect(Collectors.toList());
    }

    // Returns loans for one specific borrower
    // Used for "My Loans" page on the website
    public List<Loan> getLoansByUserId(String userId) {
        return loans.stream()
                .filter(l -> l.getId().equals(userId))
                .collect(Collectors.toList());
    }

    // Returns overdue loans for admin's red flag list in the app
    public List<Loan> getOverdueLoans() {
        String today = LocalDate.now().toString();
        return loans.stream()
                .filter(l -> l.getStatus().equals("overdue")
                && l.getDueDate().compareTo(today) < 0)
                .collect(Collectors.toList());
    }

    // Admin confirms a loan when borrower collects the book
    // holdId links this loan back to the hold it came from - dueDate is set by admin
    public Loan createLoan(String userId, String bookId, String titleId,
                           String holdId, String dueDate) {
        Loan loan = new Loan();
        loan.setId(UUID.randomUUID().toString());
        loan.setUserId(userId);
        loan.setBookId(bookId);
        loan.setTitleId(titleId);
        loan.setHoldId(holdId);
        loan.setLoanDate(LocalDate.now().toString());
        loan.setDueDate(dueDate);
        loan.setReturnDate(null); // null until book is returned
        loan.setStatus("active");
        loans.add(loan);
        return loan;
    }

    // Admin processes a return - borrower brings book back
    // Sets returnDate to today and status to returned
    public Optional<Loan> processReturn(String bookId) {
        // Find the active loan for this book
        Optional<Loan> loanOpt = loans.stream()
                .filter(l -> l.getBookId().equals(bookId)
                && l.getStatus().equals("active"))
                .findFirst();
        loanOpt.ifPresent(loan ->{
            loan.setStatus("returned");
            loan.setReturnDate(LocalDate.now().toString());
        });
        return loanOpt;
    }

    // Marks a loan as overdue
    // Called by the scheduler every night for past-due active loans
    public void markOverdue(String loanId) {
        loans.stream()
                .filter(l -> l.getId().equals(loanId))
                .findFirst()
                .ifPresent(l -> l.setStatus("overdue"));
    }

    // Finds one loan by ID
    public Optional<Loan> findById(String loanId) {
        return loans.stream()
                .filter(l -> l.getId().equals(loanId))
                .findFirst();
    }

}
