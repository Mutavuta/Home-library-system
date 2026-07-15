package com.library.admin.model;

// The data object inside ApiResponse after a successful login
public class AuthResponse {

    private String token;
    private String userId;
    private String role;
    private String fullName;
    private String status;

    public String getToken()    { return token; }
    public String getUserId()   { return userId; }
    public String getRole()     { return role; }
    public String getFullName() { return fullName; }
    public String getStatus()   { return status; }

}
