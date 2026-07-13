package com.library.librarybackend.scheduler;

import com.library.librarybackend.model.Hold;
import com.library.librarybackend.model.Loan;
import com.library.librarybackend.repository.HoldRepository;
import com.library.librarybackend.repository.LoanRepository;
import com.library.librarybackend.service.HoldService;
import com.library.librarybackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// Runs two automatic background jobs every day
// @EnableScheduling in LibraryBackendApplication is what activates these
@Component
public class HoldExpiryScheduler {

    @Autowired private HoldRepository holdRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private HoldService holdService;
    @Autowired private NotificationService notificationService;

    // How many days a borrower has to collect after their hold is approved - from application.properties
    @Value("${app.hold.expiry.days}")
    private int holdExpiryDays;

    // Runs every day at midnight (00:00)
    // Checks all approved holds and either expires them or sends a warning notification if then deadline is tomorrow
    @Scheduled(cron = "0 0 0 * * *")
    public void processHoldExpiry() {
        try {
            List<Hold> approvedHolds = holdRepository.findByStatus("approved");
            for (Hold hold : approvedHolds) {
                // Skip holds with no approvedDate - should not happen but defensive check
                if (hold.getApprovedDate() == null) continue;

                LocalDate approvedDate = LocalDate.parse(hold.getApprovedDate());
                // Expiry date = day approved + holdExpiryDays (default 3
                LocalDate expiryDate = approvedDate.plusDays(holdExpiryDays);
                // Warning sent the day before expiry
                LocalDate warningDate = expiryDate.minusDays(1);

                if (!LocalDate.now().isBefore(expiryDate)) {
                    // Today is on or after the expiry date - expire the hold
                    holdService.expireHold(hold.getId());
                } else if (LocalDate.now().isEqual(warningDate)) {
                    // Today is the warning day - remind the borrower to collect the book
                    notificationService.notifyHoldExpiring(hold.getUserId(), hold.getTitle());
                }
            }
        } catch (Exception e) {
            // Log the error but don't crash - other holds need processing
            System.err.println("[HoldExpiryScheduler] Hold expiry error: " + e.getMessage());
        }
    }

    // Runs every day at 8am
    // Checks all active loans and:
    // - Sends a due reminder if the loan is due exactly 2 days
    // _ Marks the loan as overdue if the due date has already passed
    @Scheduled(cron = "0 0 8 * * *")
    public void processDueReminders() {
        try {
            String reminderDate = LocalDate.now().plusDays(2).toString();
            String today        = LocalDate.now().toString();

            List<Loan> activeLoans = loanRepository.findAllActive();
            for (Loan loan : activeLoans) {
                if (reminderDate.equals(loan.getDueDate())) {
                    // Due in 2 days - send the borrower a reminder
                    notificationService.notifyDueReminder(
                            loan.getUserId(), loan.getTitleId(), loan.getDueDate());
                }

                // String date comparison works because format is yyyy-mm-dd (sorts correctly)
                if (loan.getDueDate() != null && loan.getDueDate().compareTo(today) < 0) {
                    // Due date has passed - mark as overdue
                    loanRepository.updatesStatus(loan.getId(), "overdue");
                }
            }
        } catch (Exception e) {
            System.err.println("[HoldExpiryScheduler] Due reminder error: " + e.getMessage());
        }
    }

}
