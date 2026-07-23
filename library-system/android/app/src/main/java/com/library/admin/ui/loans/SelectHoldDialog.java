package com.library.admin.ui.loans;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.Hold;
import com.library.admin.model.User;
import com.library.admin.utils.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Step 1 of Confirm Loan - shows every approved hold (already has a copy
// assigned) so admin can pick which one to convert into an actual loan.
public class SelectHoldDialog extends Dialog {

    public interface OnHoldSelectedListener {
        void onHoldSelected(Hold hold);
    }

    private OnHoldSelectedListener listener;
    private ApiService apiService;
    private HoldSelectAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    public SelectHoldDialog(@NonNull Context context) {
        super(context);
    }

    public void setOnHoldSelectedListener(OnHoldSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_hold);

        apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);

        recyclerView = findViewById(R.id.recyclerApprovedHolds);
        tvEmpty      = findViewById(R.id.tvEmpty);

        adapter = new HoldSelectAdapter(hold -> {
            if (listener != null) listener.onHoldSelected(hold);
            dismiss();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());

        loadApprovedHolds();
    }

    private void loadApprovedHolds() {
        apiService.getHoldsByStatus("approved").enqueue(new Callback<ApiResponse<List<Hold>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Hold>>> call, Response<ApiResponse<List<Hold>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Hold> holds = response.body().getData();
                    attachBorrowerNames(holds);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Hold>>> call, Throwable t) {
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    // Same denormalization pattern as HoldsFragment - Hold doesn't store a
    // readable name, only userId, so we fetch users once and match locally
    private void attachBorrowerNames(List<Hold> holds) {
        apiService.getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                Map<String, String> nameMap = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    for (User u : response.body().getData()) {
                        nameMap.put(u.id, u.fullName);
                    }
                }
                for (Hold h : holds) {
                    h.borrowerName = nameMap.get(h.userId);
                }
                showResults(holds);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                showResults(holds);
            }
        });
    }

    private void showResults(List<Hold> holds) {
        if (holds.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            adapter.setHolds(holds);
        }
    }
}
