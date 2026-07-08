package com.library.librarybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String passwordHash;
    private String createdAt;

}
