package com.library.admin.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BookTitle {

    // Firestore document ID
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("author")
    public String author;

    @SerializedName("category")
    public String category;

    @SerializedName("coverImageUrl")
    public String coverImageUrl;

    @SerializedName("totalCopies")
    public int totalCopies;

    @SerializedName("availableCopies")
    public int availableCopies;

    @SerializedName("reservedCopies")
    public int reservedCopies;

    @SerializedName("loanedCopies")
    public int loanedCopies;

    @SerializedName("copies")
    public List<String> copies;

}
