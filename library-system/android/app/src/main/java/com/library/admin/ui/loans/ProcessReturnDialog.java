package com.library.admin.ui.loans;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.Book;
import com.library.admin.model.Loan;
import com.library.admin.model.User;
import com.library.admin.utils.ApiErrorUtils;
import com.library.admin.utils.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Shown right after a barcode scan on "Scan to Return".
// Looks up the copy to show what's being returned, then admin confirms.
public class ProcessReturnDialog extends Dialog {

    public interface OnReturnedListener {
        void onReturned(Loan loan, Book book);
    }

    private final String scannedBarcode;
    private final FragmentManager fragmentManager;
    private OnReturnedListener listener;
    private ApiService apiService;
    private Book lookedUpBook;

    private TextInputEditText etBarcode;
    private View llBookInfo;
    private TextView tvBookTitle, tvBorrowerName, tvError;
    private Button btnConfirmReturn;

    public ProcessReturnDialog(@NonNull Context context, String scannedBarcode, FragmentManager fragmentManager) {
        super(context);
        this.scannedBarcode = scannedBarcode;
        this.fragmentManager = fragmentManager;
    }

    public void setOnReturnedListener(OnReturnedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_process_return);

        apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);

        etBarcode        = findViewById(R.id.etBarcode);
        llBookInfo       = findViewById(R.id.llBookInfo);
        tvBookTitle      = findViewById(R.id.tvBookTitle);
        tvBorrowerName   = findViewById(R.id.tvBorrowerName);
        tvError          = findViewById(R.id.tvError);
        btnConfirmReturn = findViewById(R.id.btnConfirmReturn);

        etBarcode.setText(scannedBarcode);

        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        btnConfirmReturn.setOnClickListener(v -> attemptReturn());

        lookupBook(scannedBarcode);
    }

    // Looks up the copy so admin can visually confirm what's being returned
    // before committing - reduces the chance of scanning the wrong shelf item
    private void lookupBook(String barcodeId) {
        apiService.getBookByBarcode(barcodeId).enqueue(new Callback<ApiResponse<Book>>() {
            @Override
            public void onResponse(Call<ApiResponse<Book>> call, Response<ApiResponse<Book>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    lookedUpBook = response.body().getData();

                    if (!"loaned".equals(lookedUpBook.status)) {
                        showError("This copy is not currently on loan.");
                        return;
                    }

                    tvBookTitle.setText(lookedUpBook.title);
                    llBookInfo.setVisibility(View.VISIBLE);
                    tvError.setVisibility(View.GONE);
                    lookupBorrowerName(lookedUpBook.currentHolderId);
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

    // Book only stores currentHolderId, not a readable name - fetch all users
    // once and match locally, same pattern used elsewhere in the app
    private void lookupBorrowerName(String holderId) {
        apiService.getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    for (User u : response.body().getData()) {
                        if (u.id.equals(holderId)) {
                            tvBorrowerName.setText(u.fullName);
                            return;
                        }
                    }
                }
                tvBorrowerName.setText("Unknown Borrower");
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                tvBorrowerName.setText("Unknown Borrower");
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        llBookInfo.setVisibility(View.GONE);
        btnConfirmReturn.setEnabled(false);
    }

    private void attemptReturn() {
        String barcode = etBarcode.getText() != null ? etBarcode.getText().toString().trim() : "";
        if (TextUtils.isEmpty(barcode)) return;

        Map<String, String> body = new HashMap<>();
        body.put("barcodeId", barcode);

        btnConfirmReturn.setEnabled(false);

        apiService.returnBook(body).enqueue(new Callback<ApiResponse<Loan>>() {
            @Override
            public void onResponse(Call<ApiResponse<Loan>> call, Response<ApiResponse<Loan>> response) {
                btnConfirmReturn.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    dismiss();
                    if (listener != null) listener.onReturned(response.body().getData(), lookedUpBook);
                } else {
                    Toast.makeText(getContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Loan>> call, Throwable t) {
                btnConfirmReturn.setEnabled(true);
                Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_LONG).show();
            }
        });
    }
}
