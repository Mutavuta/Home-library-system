package com.library.librarybackend;

import com.library.librarybackend.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
@RestController
public class TestController {
    @GetMapping("/hello")
    public ApiResponse<String> hello() {
        return ApiResponse.ok("Server is working", "Hello from my library backend");
    }
} */

@RestController
public class TestController {
    @GetMapping("/status")
    public ApiResponse<String> helo() {
    return ApiResponse.ok("library is online", "version 1.0");
    }
}