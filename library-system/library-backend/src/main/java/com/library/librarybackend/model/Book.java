package com.library.librarybackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    // Change the random UUID ID to the book's barcode so no separate id needed
    private String barcodeId;

    // Links this copy back to its BookTitle catalog entry
    private String titleId;

    // Duplicated from BookTitle on purpose - avoids an extra lookup whenever
    // we just need to display "which copy of which book is this"
    private String title;
    private String author;

    // available = on the shelf, reserved = held for someone, loaned = checked out
    private String status;

    // userId of whoever currently has this copy reserved or loaned - null if available
    private String currentHolderId;

    // When this copy was added to the library -yyyy-mm-dd
    private String dateAdded;

}
