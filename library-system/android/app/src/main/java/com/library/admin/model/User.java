package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

// Represents a library user - either an dmin or a borrower
public class User {

    // Firestore document ID
    private String id;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;


}
