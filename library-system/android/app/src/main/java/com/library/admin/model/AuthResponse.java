package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

// The data object inside ApiResponse after a successful login
public class AuthResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("userId")
    private String userId;

    @SerializedName("role")
    private String role;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("status")
    private String status;


}
