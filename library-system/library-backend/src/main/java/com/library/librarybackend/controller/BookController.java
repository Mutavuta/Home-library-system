package com.library.librarybackend.controller;

import com.library.librarybackend.dto.AddBookRequest;
import com.library.librarybackend.dto.ApiResponse;
import com.library.librarybackend.model.Book;
import com.library.librarybackend.model.BookTitle;
import com.library.librarybackend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController = handles HTTP requests + auto converts responses to JSON
// Handles browsing the book catalog (public) and managing physical copies (admin only)
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired private BookService bookService;

    // GET /books/titles - returns all catalog entries
    // Public - no login required and borrowers can browse books before they have an account
    @GetMapping("/titles")
    public ResponseEntity<ApiResponse<List<BookTitle>>> getAllTitles(
            @RequestParam(required = false) String category) {
        try {
            List<BookTitle> titles = category != null && !category.isBlank()
                    ? bookService.getTitlesByCategory(category)
                    : bookService.getAllTitles();
            return ResponseEntity.ok(ApiResponse.ok("OK", titles));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /books/titles/{titleId} - returns a single catalog entry
    // Public - used to show the detail page for a title
    @GetMapping("/titles/{titleId}")
    public ResponseEntity<ApiResponse<BookTitle>> getTitle(@PathVariable String titleId) {
        try {
            return bookService.getTitleById(titleId)
                    .map(t -> ResponseEntity.ok(ApiResponse.ok("OK", t)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /books/titles/{titleId}/copies -returns all physical copies of a title
    @GetMapping("/titles/{titleId}/copies")
    public ResponseEntity<ApiResponse<List<Book>>> getCopies(@PathVariable String titleId) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("OK", bookService.getCopiesByTitleId(titleId)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /books/admin/add -admin adds a new physical copy via barcode scan
    // If no titleId is provided a new catalog entry is created automatically
    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Book>> addBook(@RequestBody AddBookRequest req) {
        try {
            Book book = bookService.addBook(req);
            return ResponseEntity.ok(ApiResponse.ok("Book added successfully", book));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /books/admin/all - returns every physical copy in the library
    // Used by admin for full inventory management
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks() {
        try {
            return ResponseEntity.ok(ApiResponse.ok("OK", bookService.getAllBooks()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /books/barcode/{barcodeId} - looks up a single copy by its barcodeId
    // Used admin scans a book in the Android app
    @GetMapping("/barcode/{barcodeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Book>> getByBarcode(@PathVariable String barcodeId) {
        try {
            return bookService.getBookByBarcode(barcodeId)
                    .map(b -> ResponseEntity.ok(ApiResponse.ok("OK", b)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    // DELETE /books/admin/{barcodeId} - removes a physical copy from the library
    // Only allowed if the copy is currently available - not reserved
    @DeleteMapping("/admin/{barcodeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String barcodeId) {
        try {
            bookService.deleteBook(barcodeId);
            return ResponseEntity.ok(ApiResponse.ok("Book deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}