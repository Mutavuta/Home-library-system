package com.library.librarybackend.service;

import com.library.librarybackend.model.Book;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {

    private List<Book> books = new ArrayList<>();

    public List<Book> getAllBooks() {
        return books;
    }

    public Book addBook(Book book) {
        book.setId(UUID.randomUUID().toString());
        books.add(book);
        return book;
    }

    public Optional<Book> getBookById(String id) {
        return books.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst();
    }

    public boolean deleteBook(String id) {
        return books.removeIf(b -> b.getId().equals(id));
    }

    public List<Book> getBooksByStatus(String status) {
        return books.stream()
                .filter(b -> b.getStatus().equals(status))
                .toList();
    }
}
