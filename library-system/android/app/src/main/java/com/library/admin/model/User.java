package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

// Represents a library user - either an dmin or a borrower
public class User {

    // Firestore document ID
    public String id;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("phone")
    public String phone;

    @SerializedName("role")
    public String role;

    @SerializedName("status")
    public String status;

    @SerializedName("createdAt")
    public String createdAt;


}
