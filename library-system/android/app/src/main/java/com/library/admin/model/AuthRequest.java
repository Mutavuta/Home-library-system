package com.library.admin.model;

// Request body sen to POST /auth/login
public class AuthRequest {

    private String email;
    private String password;

    public AuthRequest(String email, String password) {
        this.email    = email;
        this.password = password;
    }

}
