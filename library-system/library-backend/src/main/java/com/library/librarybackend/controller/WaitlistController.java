package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.WaitlistEntry;
import com.library.librarybackend.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Handles all waitlist-related HTTP requests
// Waitlist = queue  of borrowers waiting for a book with no  available copies
@RestController
@RequestMapping("/waitlist")
public class WaitlistController {

    // Spring injects WaitlistService automatically
    @Autowired
    private WaitlistService waitlistService;

    // GET /waitlist/title/{titleId} - gets the queue for one book title
    // Shows who is waiting and what position they are in
    @GetMapping("/title/{titleId}")
    public ApiResponse<List<WaitlistEntry>> getWaitlistForTitle(
            @PathVariable String titleId) {
        return ApiResponse.ok("Waitlist fetched",
                waitlistService.getWaitlistForTitle(titleId));
    }

    // GET /waitlist/user/{userId} - gets all waitlist entries for one user
    // Website uses this to show borrower which titles they are waiting for
    @GetMapping("/user/{userId}")
    public ApiResponse<List<WaitlistEntry>> getWaitlistForUser(
            @PathVariable String userId) {
        return ApiResponse.ok("User waitlist",
                waitlistService.getWaitlistForUser(userId));
    }

    // GET /waitlist/title/{titleId}/count -how many people are waiting
    // Used to show borrower their position before they join
    @GetMapping("/title/{titleId}/count")
    public ApiResponse<Integer> getWaitingCount(@PathVariable String titleId) {
        return ApiResponse.ok("Waiting count",
                waitlistService.countWaiting(titleId));
    }

    // POST /waitlist - borrower joins the waitlist for a title
    // Body must contain: userId, titleId
    @PostMapping
    public ApiResponse<WaitlistEntry> joinWaitlist(
            @RequestBody Map<String, String> body) {

        String userId  = body.get("userId");
        String titleId = body.get("titleId");

        // Validate Required fields
        if (userId == null || titleId == null) {
            return ApiResponse.error("userId and titleId are required");
        }

        // Prevent the same person joining the same waitlist twice
        if (waitlistService.isAlreadyWaiting(userId, titleId)) {
            return ApiResponse.error(
                    "You are already on the waitlist for this book");
        }

        WaitlistEntry entry = waitlistService.addToWaitlist(userId, titleId);

        return ApiResponse.ok(
                "Added to waitlist at position " + entry.getPosition(),entry);
    }

}
