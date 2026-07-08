package com.library.librarybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// What the server sends back after a successful login
// The client stores the token and sends it in the Authorization header on every
// subsequent request - "Bearer <token>"
@Data
@AllArgsConstructor
public class AuthResponse {

    // The JWT token - everything the server needs is encoded inside this
    private String token;

    // Stored client-side so the app knows whose session this is
    private String userId;

    // "admin" or "borrower" - controls which screens the app/website shows
    private String role;

    // Shown in the app header - e.g "Welcome back, Paul"
    private String fullName;

    // "pending" or approved - the website shows a "pending approval" banner if pending
    private String status;

}
