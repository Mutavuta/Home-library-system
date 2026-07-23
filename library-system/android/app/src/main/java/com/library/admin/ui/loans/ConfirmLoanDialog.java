package com.library.admin.ui.loans;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.Hold;
import com.library.admin.model.Loan;
import com.library.admin.utils.ApiErrorUtils;
import com.library.admin.utils.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Step 2 of Confirm Loan - shows the picked hold's details, lets admin verify
// the barcode and pick a due date, then calls POST /loans/admin/approve.
public class ConfirmLoanDialog extends Dialog {

    public interface OnLoanConfirmedListener {
        void onLoanConfirmed();
    }

    private final Hold hold;
    private final FragmentManager fragmentManager; // needed to show MaterialDatePicker
    private OnLoanConfirmedListener listener;
    private ApiService apiService;

    private TextInputEditText etBarcode, etDueDate;

    public ConfirmLoanDialog(@NonNull Context context, Hold hold, FragmentManager fragmentManager) {
        super(context);
        this.hold = hold;
        this.fragmentManager = fragmentManager;
    }

    public void setOnLoanConfirmedListener(OnLoanConfirmedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm_loan);

        apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);

        ((android.widget.TextView) findViewById(R.id.tvBorrowerName))
                .setText(hold.borrowerName != null ? hold.borrowerName : "Unknown Borrower");
        ((android.widget.TextView) findViewById(R.id.tvBookTitle)).setText(hold.title);

        etBarcode = findViewById(R.id.etBarcode);
        etDueDate = findViewById(R.id.etDueDate);

        // Pre-fill with the copy already assigned during hold approval -
        // admin can still edit this if the scanned copy differs
        etBarcode.setText(hold.assignedBarcodeId);

        etDueDate.setOnClickListener(v -> openDatePicker());

        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.btnConfirm).setOnClickListener(v -> attemptConfirm());
    }

    private void openDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select due date")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // MaterialDatePicker returns UTC millis - force UTC formatting to avoid
            // the date shifting by a day depending on the device's local timezone
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            etDueDate.setText(sdf.format(selection));
        });

        picker.show(fragmentManager, "due_date_picker");
    }

    private void attemptConfirm() {
        String barcode = etBarcode.getText() != null ? etBarcode.getText().toString().trim() : "";
        String dueDate = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";

        if (TextUtils.isEmpty(barcode) || TextUtils.isEmpty(dueDate)) {
            Toast.makeText(getContext(), R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("holdId", hold.id);
        body.put("barcodeId", barcode);
        body.put("dueDate", dueDate);

        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setEnabled(false);

        apiService.approveLoan(body).enqueue(new Callback<ApiResponse<Loan>>() {
            @Override
            public void onResponse(Call<ApiResponse<Loan>> call, Response<ApiResponse<Loan>> response) {
                btnConfirm.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), R.string.msg_loan_confirmed, Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onLoanConfirmed();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Loan>> call, Throwable t) {
                btnConfirm.setEnabled(true);
                Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_LONG).show();
            }
        });
    }
}
