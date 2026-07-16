package com.library.admin.model;

import com.google.gson.annotations.SerializedName;

public class Hold {

    public String id;

    @SerializedName("userId")
    public String userId;

    @SerializedName("titleId")
    public String titleId;

    @SerializedName("title")
    public String title;

    @SerializedName("requestDate")
    public String requestDate;

    @SerializedName("approvedDate")
    public String approvedDate;

    @SerializedName("status")
    public String status;

    @SerializedName("assignedBarcodeId")
    public String assignedBarcodeId;

    @SerializedName("reservationLocked")
    public boolean reservationLocked;

    // The borrower's full name - fetched separately and set locally for display
    // Not stored in Firestore - populated by HoldsFragment after fetching user details
    public String borrowerName;

}
