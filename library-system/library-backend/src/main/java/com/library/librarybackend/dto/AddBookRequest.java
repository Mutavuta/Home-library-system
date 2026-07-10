package com.library.librarybackend.dto;

import lombok.Data;

// Request body shape for POST /api/books
// Keeps input data separate from Book model itself -
// so the caller can't accidentally set fields like status or currentHolderId
@Data
public class AddBookRequest {

    // The barcode printed on the physical sticker you put on the book
    private String barcodeId;

    private String title;
    private String author;
    private String category;

    // Optional - if this copy belongs to an existing BookTitle in the catalog
    // Leave null if this is the first copy of a brand new title
    private String titleId;

    // Optional - URL to a cover image, used on the browse page
    private String coverImageUrl;

}
