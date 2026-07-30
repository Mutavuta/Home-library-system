package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

public class Loan {

    public String id;

    @SerializedName("userId")
    public String userId;

    @SerializedName("barcodeId")
    public String barcodeId;

    @SerializedName("titleId")
    public String titleId;

    @SerializedName("holdId")
    public String holdId;

    @SerializedName("loanDate")
    public String loanDate;

    @SerializedName("dueDate")
    public String dueDate;

    @SerializedName("returnDate")
    public String returnDate;

    @SerializedName("status")
    public String status;

    // These three fields are not stored in Firestore
    // They are fetched separately and set locally for display on the loans screen
    public String borrowerName;
    public String borrowerEmail;
    public String bookTitle;

}
