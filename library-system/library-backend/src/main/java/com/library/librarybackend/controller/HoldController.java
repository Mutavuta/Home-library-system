package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.Hold;
import com.library.librarybackend.service.HoldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Handles all hold-related HHTP requests
@RestController
@RequestMapping("/holds")
public class HoldController {

    @Autowired private HoldService holdService;

    // POST /holds - borrower places a hold on title
    // If copies are available a pending hold is created if not the user is added to the waitlist
    // auth.getPrincipal() givees us the userId that JwtAuthFilter stamped on the request
    @PostMapping
    public ResponseEntity<ApiResponse<Hold>> placeHold(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            String userId = (String) auth.getPrincipal();
            Hold hold = holdService.placeHold(userId, body.get("titleId"));
            return ResponseEntity.ok(ApiResponse.ok("Hold placed successfully", hold));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /holds/mine - returns all holds for the logged-in borrower
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<Hold>>> myHolds(Authentication auth) {
        try {
            String userId = (String) auth.getPrincipal();
            return ResponseEntity.ok(ApiResponse.ok("OK", holdService.getUserHolds(userId)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // DELETE /holds/{holdId} - borrower cancels their own pending hold
    @DeleteMapping("/{holdId}")
    public ResponseEntity<ApiResponse<Void>> cancelHold(
            @PathVariable String holdId, Authentication auth) {
        try {
            String userId = (String) auth.getPrincipal();
            holdService.canceHold(holdId, userId);
            return ResponseEntity.ok(ApiResponse.ok("Hold cancelled", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /holds/admin/all -returns all holds in the system
    // Optional ?status=pending filters by status
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Hold>>> allHolds(
            @RequestParam(required = false) String status) {
        try {
            List<Hold> holds = status != null
                    ? holdService.getHoldByStatus(status)
                    : holdService.getAllHolds();
            return ResponseEntity.ok(ApiResponse.ok("OK", holds));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /holds/admin/{holdId}/approve - admin asigns a copy to a pending hold
    @PostMapping("/admin/{holdId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Hold>> approveHold(
            @PathVariable String holdId,
            @RequestBody Map<String, String> body) {
        try {
            Hold hold = holdService.approveHold(holdId, body.get("barcodeId"));
            return ResponseEntity.ok(ApiResponse.ok("Hold approved", hold));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));

        }
    }

    // POST /holds/admin/{holdId}/expire - admin manually expires a hold
    // Used when borrower did not collect within the allowed days
    @PostMapping("/admin/{holdId}/expire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> expireHold(@PathVariable String holdId) {
        try {
            holdService.expireHold(holdId);
            return ResponseEntity.ok(ApiResponse.ok("Hold expired", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}
