package com.library.librarybackend.dto;

import lombok.Data;

// Request body shape for POST /api/auth/login
// Only email and password - nothing else should come from the client on login
@Data
public class AuthRequest {

    // The email the borrower registered with
    private String email;

    // Plain-text password - compared against the hashed version stored in Firestore
    private String password;

}
