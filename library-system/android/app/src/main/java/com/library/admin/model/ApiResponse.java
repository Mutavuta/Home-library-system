package com.library.admin.model;

// Mirrors the ApiResponse<T> wrapper the Spring Boot backend sends on every response
public class ApiResponse<T> {

    // True if the request succeeded, false if something went wrong
    private boolean success;

    // Human-readable message - shown in error toasts
    private String message;

    // The actual payload - a User, List<Hold>, etc. null
    private T data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData()         { return data; }

}
