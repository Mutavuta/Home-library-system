package com.library.admin.api;

import com.library.admin.model.ApiResponse;
import com.library.admin.model.AuthRequest;
import com.library.admin.model.AuthResponse;
import com.library.admin.model.BookTitle;
import com.library.admin.model.Book;
import com.library.admin.model.Hold;
import com.library.admin.model.Loan;
import com.library.admin.model.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Defines every backend API call the app can make
// Retrofit reads these method signatures and builds the actual HTTP calls automatically
public interface ApiService {

    // Auth
    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body AuthRequest request);

    @POST("auth/bootstrap-admin")
    Call<ApiResponse<User>> bootstrapAdmin(@Body Map<String, String> body);

    @POST("auth/create-admin")
    Call<ApiResponse<User>> createAdmin(@Body Map<String, String> body);

    // Books
    @GET("book/titles")
    Call<ApiResponse<List<BookTitle>>> getAllTitles();

    @GET("book/titles")
    Call<ApiResponse<List<BookTitle>>> getTitlesByCategory(@Query("category") String category);

    @GET("book/admin/all")
    Call<ApiResponse<List<Book>>> getAllBooks();

    @GET("book/barcode/{barcodeId}")
    Call<ApiResponse<Book>> getBookByBarcode(@Path("barcodeId") String barcodeId);

    @POST("book/admin/add")
    Call<ApiResponse<Book>> addBook(@Body Map<String, String> body);

    @DELETE("book/admin/{barcodeId}")
    Call<ApiResponse<Void>> deleteBook(@Path("barcodeId") String barcodeId);

    // Holds
    @GET("holds/admin/all")
    Call<ApiResponse<List<Hold>>> getAllHolds();

    @GET("holds/admin/all")
    Call<ApiResponse<List<Hold>>> getHoldsByStatus(@Query("status") String status);

    @POST("holds/admin/{holdId}/approve")
    Call<ApiResponse<Hold>> approveHold(@Path("holdId") String holdId, @Body Map<String, String> body);

    @POST("holds/admin/{holdId}/expire")
    Call<ApiResponse<Void>> expireHold(@Path("holdId") String holdId);

    // Loans
    @GET("loans/admin/active")
    Call<ApiResponse<List<Loan>>> getActiveLoans();

    @GET("loans/admin/overdue")
    Call<ApiResponse<List<Loan>>> getOverdueLoans();

    @GET("loans/admin/all")
    Call<ApiResponse<List<Loan>>> getAllLoans();

    @POST("loans/admin/approve")
    Call<ApiResponse<Loan>> approveLoan(@Body Map<String, String> body);

    @POST("loans/admin/return")
    Call<ApiResponse<Loan>> returnBook(@Body Map<String, String> body);

    // Users
    @GET("users/admin/all")
    Call<ApiResponse<List<User>>> getAllUsers();

    @GET("users/admin/pending")
    Call<ApiResponse<List<User>>> getPendingUsers();

    @POST("users/admin/{userId}/approve")
    Call<ApiResponse<User>> approveUser(@Path("userId") String userId);

    @POST("users/admin/{userId}/suspend")
    Call<ApiResponse<User>> suspendUser(@Path("userId") String userId);

    @POST("users/admin/{userId}/reactivate")
    Call<ApiResponse<User>> reactivateUser(@Path("userId") String userId);

    // Dashboard stats
    // These reuse existing endpoints - dashboard aggregates their results
    @GET("loans/admin/overdue")
    Call<ApiResponse<List<Loan>>> getOverdueLoansForStats();

    @GET("book/titles")
    Call<ApiResponse<List<BookTitle>>> getTitlesForStats();

    @GET("books/admin/all")
    Call<ApiResponse<List<Book>>> getAllBooksForStats();

    @GET("users/admin/pending")
    Call<ApiResponse<List<User>>> getPendingUsersForStats();

    @GET("users/admin/all")
    Call<ApiResponse<List<User>>> getAllUsersForStats();
}
