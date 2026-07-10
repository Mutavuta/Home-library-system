package com.library.librarybackend.service;

import com.library.librarybackend.dto.AddBookRequest;
import com.library.librarybackend.model.Book;
import com.library.librarybackend.model.BookTitle;
import com.library.librarybackend.repository.BookRepository;
import com.library.librarybackend.repository.BookTitleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

// Handles adding, retrieving and deleting books and their title catalog entries
@Service
public class BookService {

    @Autowired private BookRepository bookRepository;
    @Autowired private BookTitleRepository bookTitleRepository;

    // Adds a new physical copy to the library
    // If a titleId is provided it links to an existing catalog entry
    // If not it creates a new catalog entry (BookTitle) first then adds the copy
    public Book addBook(AddBookRequest req) throws ExecutionException, InterruptedException {
        // Barcodes must be unique
        if (bookRepository.findById(req.getBarcodeId()).isPresent()) {
            throw new RuntimeException("A book with barcode" + req.getBarcodeId() + " already exists.");
        }

        BookTitle bookTitle;
        if (req.getTitleId() != null && !req.getTitleId().isEmpty()) {
            // Link thi copy to an existing title group in the catalog
            bookTitle = bookTitleRepository.findById(req.getTitleId())
                    .orElseThrow(() -> new RuntimeException("Title group not found"));
        } else {
            // No titleId given - create new catalog entry for this title
            bookTitle = new BookTitle();
            bookTitle.setTitle(req.getTitle());
            bookTitle.setAuthor(req.getAuthor());
            bookTitle.setCategory(req.getCategory());
            bookTitle.setCoverImageUrl(req.getCoverImageUrl());
            bookTitle.setTotalCopies(0);
            bookTitle.setAvailableCopies(0);
            bookTitle.setReservedCopies(0);
            bookTitle.setLoanedCopies(0);
            // Empty list - copies get added below
            bookTitle.setCopies(new ArrayList<>());
            bookTitle = bookTitleRepository.save(bookTitle);
        }

        // Create the physical copy record using the barcode as its identity
        Book book = new Book();
        book.setBarcodeId(req.getBarcodeId());
        book.setTitleId(bookTitle.getId());
        // Duplicate from title for fast display without extra lookups
        book.setTitle(bookTitle.getTitle());
        book.setAuthor(bookTitle.getAuthor());
        book.setStatus("available");
        book.setCurrentHolderId(null);
        book.setDateAdded(LocalDate.now().toString());
        bookRepository.save(book);

        // Add barcode to the title's copies array and recalculate counts
        bookTitleRepository.addCopyToTitle(bookTitle.getId(), req.getBarcodeId());
        refreshTitleCounts(bookTitle.getId());

        return book;
    }

    // Recalculates and saves the copy counts on a BookTitle
    // Called any time a copy's status changes - keeps catalog counts always accurate
    public void refreshTitleCounts(String titleId) throws ExecutionException, InterruptedException {
        List<Book> copies = bookRepository.findByTitleId(titleId);
        int totalCopies     = copies.size();
        int availableCopies = (int) copies.stream().filter(b -> "available".equals(b.getStatus())).count();
        int reservedCopies  = (int) copies.stream().filter(b -> "reserved".equals(b.getStatus())).count();
        int loanedCopies    = (int) copies.stream().filter(b -> "loaned".equals(b.getStatus())).count();
        bookTitleRepository.updateCopyCounts(titleId, totalCopies, availableCopies, reservedCopies, loanedCopies);
    }

    // Returns all catalog  entries - used on the browser page
    public List<BookTitle> getAllTitles() throws ExecutionException, InterruptedException {
        return bookTitleRepository.findAll();
    }

    // Finds a single catalog entry by id - used to load a title detail page
    public Optional<BookTitle> getTitleById(String id) throws ExecutionException, InterruptedException {
        return bookTitleRepository.findById(id);
    }

    // Returns titles filtered by category - used for category browsing
    public List<BookTitle> getTitlesByCategory(String category) throws ExecutionException, InterruptedException {
        return bookTitleRepository.findByCategory(category);
    }

    // Returns all physical copies - used by admin for full inventory
    public List<Book> getAllBooks() throws ExecutionException, InterruptedException {
        return bookRepository.findAll();
    }

    // Finds a single copy by barcode - used when admin scans a book in The Android app
    public Optional<Book> getBookByBarcode(String barcodeId) throws ExecutionException, InterruptedException {
        return bookRepository.findById(barcodeId);
    }

    // Returns all copies belonging to a title - shown on the title detail page
    public List<Book> getCopiesByTitleId(String titleId) throws ExecutionException, InterruptedException {
        return bookRepository.findByTitleId(titleId);
    }

    // Deletes a physical copy - only allowed if the copy is currently available
    // Also removes it from the title's copies array and refreshes counts
    public void deleteBook(String barcodeId) throws ExecutionException, InterruptedException {
        Book book = bookRepository.findById(barcodeId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        // Prevent deleting a copy that is currently with a borrower
        if (!"available".equals(book.getStatus())) {
            throw new RuntimeException("Cannot delete a book that is currently reserved or loaned.");
        }
        bookTitleRepository.removeCopyFromTitle(book.getTitleId(), barcodeId);
        bookRepository.delete(barcodeId);
        refreshTitleCounts(book.getTitleId());
    }

}
