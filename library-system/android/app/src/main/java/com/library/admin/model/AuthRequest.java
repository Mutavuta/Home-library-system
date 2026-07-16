package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

// Request body sen to POST /auth/login
public class AuthRequest {

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    public AuthRequest(String email, String password) {
        this.email    = email;
        this.password = password;
    }

}
