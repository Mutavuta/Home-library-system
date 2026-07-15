package com.library.admin.model;


public class Book {

    private String barcodeId;
    private String titleId;
    private String title;
    private String author;
    private String status;
    private String currentHolderId;
    private String dateAdded;

    public String getBarcodeId()       { return barcodeId; }
    public String getTitleId()         { return titleId; }
    public String getTitle()           { return title; }
    public String getAuthor()          { return author; }
    public String getStatus()          { return status; }
    public String getCurrentHolderId() { return currentHolderId; }
    public String getDateAdded()       { return dateAdded; }

}
