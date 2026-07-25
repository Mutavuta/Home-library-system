package com.library.admin.ui.books;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.Book;
import com.library.admin.utils.ApiErrorUtils;
import com.library.admin.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Shown after scanning a barcode on "Delete Book".
// Looks up the copy first so admin can visually confirm before deleting -
// the backend itself also rejects deletion of anything not currently available.
public class DeleteBookDialog extends Dialog {

    public interface OnBookDeletedListener {
        void onBookDeleted();
    }

    private final String scannedBarcode;
    private OnBookDeletedListener listener;
    private ApiService apiService;

    private View llBookInfo;
    private TextView tvBookTitle, tvAuthor, tvBarcode, tvError;
    private Button btnDelete;

    public DeleteBookDialog(@NonNull Context context, String scannedBarcode) {
        super(context, R.style.Theme_HomeLibraryAdmin_Dialog);
        this.scannedBarcode = scannedBarcode;
    }

    public void setOnBookDeletedListener(OnBookDeletedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete_book);

        apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);

        llBookInfo  = findViewById(R.id.llBookInfo);
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvAuthor    = findViewById(R.id.tvAuthor);
        tvBarcode   = findViewById(R.id.tvBarcode);
        tvError     = findViewById(R.id.tvError);
        btnDelete   = findViewById(R.id.btnDelete);

        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        btnDelete.setOnClickListener(v -> attemptDelete());

        lookupBook(scannedBarcode);
    }

    // Looks up the copy first so admin can see exactly what they are about to delete
    private void lookupBook(String barcodeId) {
        apiService.getBookByBarcode(barcodeId).enqueue(new Callback<ApiResponse<Book>>() {
            @Override
            public void onResponse(Call<ApiResponse<Book>> call, Response<ApiResponse<Book>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Book book = response.body().getData();

                    tvBookTitle.setText(book.title);
                    tvAuthor.setText("By " + book.author);
                    tvBarcode.setText("Barcode: " + book.barcodeId);
                    llBookInfo.setVisibility(View.VISIBLE);

                    if (!"available".equals(book.status)) {
                        showError("This copy is currently " + book.status + " and cannot be deleted.");
                    } else {
                        tvError.setVisibility(View.GONE);
                        btnDelete.setEnabled(true);
                    }
                } else {
                    showError("No book found with this barcode.");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Book>> call, Throwable t) {
                showError("Could not reach the server.");
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        btnDelete.setEnabled(false);
    }

    private void attemptDelete() {
        btnDelete.setEnabled(false);

        apiService.deleteBook(scannedBarcode).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), R.string.msg_book_deleted, Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onBookDeleted();
                    dismiss();
                } else {
                    btnDelete.setEnabled(true);
                    Toast.makeText(getContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                btnDelete.setEnabled(true);
                Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_LONG).show();
            }
        });
    }

}
