package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

// The data object inside ApiResponse after a successful login
public class AuthResponse {

    @SerializedName("token")
    public String token;

    @SerializedName("userId")
    public String userId;

    @SerializedName("role")
    public String role;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("status")
    public String status;


}
