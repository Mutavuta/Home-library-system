package com.library.librarybackend.dto;

import lombok.Data;

// Request body shape for POST /api/auth/register
// Separate from User model so the client can never set role, status, or id
// Those are always forced server-side in AuthService
@Data
public class RegisterRequest {

    // Full name shown in the app and on loan records
    private String fullName;

    // Used as login username - must be unique
    private String email;

    // Optional contact for the librarian
    private String phone;

    // Plain-text password sent once - hashed and stored, never returned
    private String password;

}
