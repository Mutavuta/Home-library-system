package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.Hold;
import com.library.librarybackend.service.HoldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Handles all hold-related HHTP requests
@RestController
@RequestMapping("/holds")
public class HoldController {

    // Spring injects HoldService automatically
    @Autowired
    private HoldService holdService;

    // GET /holds - returns all books
    @GetMapping
    public ApiResponse<List<Hold>> getAllHolds() {
        return ApiResponse.ok("Holds fetched", holdService.getAllHolds());
    }

    // GET /holds/pending - returns only pending holds - used by admin to see what needs approval
    @GetMapping("/pending")
    public ApiResponse<List<Hold>> getPendingHolds() {
        return ApiResponse.ok("pending holds",
                holdService.getHoldsByStatus("pending"));
    }

    // GET /holds/user/{userId} - returns holds for one borrower - for MyHolds on the website
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Hold>> getHoldsByUser(@PathVariable String userId) {
        return ApiResponse.ok("User holds",
                holdService.getHoldsByUserId(userId));
    }

    // POST /holds - borrower places a new hold and body must contain userId, titleId, title
    @PostMapping
    public ApiResponse<Hold> placeHold(@RequestBody Map<String, String> body) {
        // Read values from the request body map
        String userId = body.get("userId");
        String titleId = body.get("titleId");
        String title = body.get("title");

        // Validate all required fields are present
        if (userId == null || titleId == null || title == null) {
            return ApiResponse.error("userId, titleId and title are required");
        }

        Hold hold = holdService.placeHold(userId, titleId, title);
        return ApiResponse.ok("Hold placed successfully", hold);
    }

    // PUT /holds/{id}/approve - admin approves a hold
    // Body must contain: bookId (the specific copy being assigned
    @PutMapping("/{id}/approve")
    public ApiResponse<Hold> approveHold(@PathVariable String id,
                                         @RequestBody Map<String, String> body){
        String bookId = body.get("bookId");
        // Check if the bookId is field
        if (bookId == null) {
            ApiResponse.error("bookId is required");
        }

        return holdService.approveHold(id, bookId)
                .map(hold -> ApiResponse.ok("Hold approved", hold))
                .orElse(ApiResponse.error("Hold not found"));
    }

    // PUT /hold/{id}/collect - marks a hold collected - called when borrower picks up the book
    @PutMapping("/{id}/collect")
    public ApiResponse<Hold> markCollected(@PathVariable String id) {
        return holdService.markCollected(id)
                .map(hold -> ApiResponse.ok("Hold marked collected", hold))
                .orElse(ApiResponse.error("Hold not found"));
    }

    // PUT /holds/{id}/abandon - cancels a hold
    // Called by then scheduler when hold expires
    @PutMapping("/{id}/abandon")
    public ApiResponse<Hold> abandonHold(@PathVariable String id) {
        return holdService.abandonHold(id)
                .map(hold -> ApiResponse.ok("Hold abandoned", hold))
                .orElse(ApiResponse.error("Hold not found"));
    }

}
