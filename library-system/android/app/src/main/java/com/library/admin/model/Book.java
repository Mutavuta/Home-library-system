package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

public class Book {

    @SerializedName("barcodeId")
    public String barcodeId;

    @SerializedName("titleId")
    public String titleId;

    @SerializedName("title")
    public String title;

    @SerializedName("author")
    public String author;

    @SerializedName("status")
    public String status;

    @SerializedName("currentHolderId")
    public String currentHolderId;

    @SerializedName("dateAdded")
    public String dateAdded;


}
