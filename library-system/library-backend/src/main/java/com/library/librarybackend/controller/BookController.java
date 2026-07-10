package com.library.librarybackend.controller;

import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.Book;
import com.library.librarybackend.service.BookService;
// @Autowired tells Spring to inject the BookService automatically
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController = handles HTTP requests + auto converts responses to JSON
// @RequestMapping("/books") = all endpoints here start with /books
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public ApiResponse<List<Book>> getAllBooks() {
        return ApiResponse.ok("Books fetched", bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> getBookById(@PathVariable String id) {
        return bookService.getBookByBarcode(id)
                .map(book -> ApiResponse.ok("Book found",book))
                .orElse(ApiResponse.error("Book not found"));
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<Book>> getByStatus(@PathVariable String status) {
        return ApiResponse.ok("Books Fetched", bookService.getBooksByStatus(status));
    }

    @PostMapping
    public ApiResponse<Book> addBook(@RequestBody Book book) {
        return ApiResponse.ok("Book added", bookService.addBook(book));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable String id) {
        boolean deleted = bookService.deleteBook(id);
        if (deleted) return ApiResponse.ok("Book deleted", null);
        return ApiResponse.error("Book not found");
    }
}