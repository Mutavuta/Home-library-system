package com.library.librarybackend.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

// Represents a CATALOG entry - "The Greedy Hyena" as a concept,
// regardless of how many physical copies the library owns.
// This is what borrowers browse on the website
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookTitle {

    // Firebase document ID
    private String id;

    private String title;
    private String author;

    // Used for filtering/browsing - e.g fiction, non-fiction
    private String category;

    // These four counts are kept in sync by BookService.refreshTitleCounts()
    // every time a copy's status changes, so the website never has to calculate them from scratch
    private int totalCopies;
    private int availableCopies;
    private int reservedCopies;
    private int loanedCopies;

    // Array of barcodeIds - every physical Book copy that belongs to this title
    private List<String> copies;

    private String coverImageUrl;

}
