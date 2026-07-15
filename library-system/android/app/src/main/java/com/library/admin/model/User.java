package com.library.admin.model;

// Represents a library user - either an dmin or a borrower
public class User {

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String createdAt;

    public String getId()       { return id; }
    public String getFullName() { return fullName; }
    public String getEmail()    { return email; }
    public String getPhone()    { return phone; }
    public String getRole()     { return role; }
    public String getStatus()   { return status; }
    public String getCreatedAt(){ return createdAt; }

}
