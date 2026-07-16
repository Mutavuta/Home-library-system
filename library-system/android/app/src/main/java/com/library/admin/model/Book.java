package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

public class Book {

    @SerializedName("barcodeId")
    private String barcodeId;

    @SerializedName("titleId")
    private String titleId;

    @SerializedName("title")
    private String title;

    @SerializedName("author")
    private String author;

    @SerializedName("status")
    private String status;

    @SerializedName("currentHolderId")
    private String currentHolderId;

    @SerializedName("dateAdded")
    private String dateAdded;


}
