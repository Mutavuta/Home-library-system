package com.library.admin.ui.holds;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.Hold;
import com.library.admin.model.User;
import com.library.admin.utils.ApiErrorUtils;
import com.library.admin.utils.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Hold Management screen - Pending/Approved/All tabs.
// Approving a hold requires assigning a physical copy, so tapping Approve
// opens the barcode scanner directly rather than a separate form.
public class HoldsFragment extends Fragment implements HoldAdapter.OnHoldActionListener {

    private Button tabPending, tabApproved, tabAll;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerHolds;

    private HoldAdapter adapter;
    private ApiService apiService;
    private String currentFilter = "pending"; // null means "All"
    private Hold holdBeingApproved; // holds the target hold between scan launch and result

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null && holdBeingApproved != null) {
                    submitApproval(holdBeingApproved, result.getContents());
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchScanner();
                } else {
                    Toast.makeText(requireContext(), R.string.error_camera_permission, Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_holds, container, false);

        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        tabPending   = view.findViewById(R.id.tabPending);
        tabApproved  = view.findViewById(R.id.tabApproved);
        tabAll       = view.findViewById(R.id.tabAll);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerHolds = view.findViewById(R.id.recyclerHolds);

        adapter = new HoldAdapter(this);
        recyclerHolds.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerHolds.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadHolds);

        tabPending.setOnClickListener(v -> {
            currentFilter = "pending";
            setActiveTab(tabPending, tabApproved, tabAll);
            loadHolds();
        });
        tabApproved.setOnClickListener(v -> {
            currentFilter = "approved";
            setActiveTab(tabApproved, tabPending, tabAll);
            loadHolds();
        });
        tabAll.setOnClickListener(v -> {
            currentFilter = null;
            setActiveTab(tabAll, tabPending, tabApproved);
            loadHolds();
        });

        loadHolds();

        return view;
    }

    private void setActiveTab(Button active, Button... others) {
        int black = ContextCompat.getColor(requireContext(), R.color.black);
        int white = ContextCompat.getColor(requireContext(), R.color.white);
        int darkText = ContextCompat.getColor(requireContext(), R.color.dark_text);

        active.setBackgroundTintList(ColorStateList.valueOf(black));
        active.setTextColor(white);

        for (Button b : others) {
            b.setBackgroundTintList(ColorStateList.valueOf(white));
            b.setTextColor(darkText);
        }
    }

    // Loads holds for the current tab, then fetches all users so we can
    // attach a readable borrower name onto each hold before displaying
    private void loadHolds() {
        Call<ApiResponse<List<Hold>>> call = currentFilter != null
                ? apiService.getHoldsByStatus(currentFilter)
                : apiService.getAllHolds();

        call.enqueue(new Callback<ApiResponse<List<Hold>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Hold>>> call, Response<ApiResponse<List<Hold>>> response) {
                swipeRefresh.setRefreshing(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Hold> holds = response.body().getData();
                    attachBorrowerNames(holds);
                } else {
                    Toast.makeText(requireContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Hold>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (isAdded()) Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachBorrowerNames(List<Hold> holds) {
        apiService.getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (!isAdded()) return;

                Map<String, String> nameMap = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    for (User u : response.body().getData()) {
                        nameMap.put(u.id, u.fullName);
                    }
                }
                for (Hold h : holds) {
                    h.borrowerName = nameMap.get(h.userId);
                }
                adapter.setHolds(holds);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                // Show holds anyway even if names failed to load - graceful degradation
                if (isAdded()) adapter.setHolds(holds);
            }
        });
    }

    @Override
    public void onApprove(Hold hold) {
        holdBeingApproved = hold;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan the copy to assign to this hold");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void submitApproval(Hold hold, String barcodeId) {
        Map<String, String> body = new HashMap<>();
        body.put("barcodeId", barcodeId);

        apiService.approveHold(hold.id, body).enqueue(new Callback<ApiResponse<Hold>>() {
            @Override
            public void onResponse(Call<ApiResponse<Hold>> call, Response<ApiResponse<Hold>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), R.string.msg_hold_approved, Toast.LENGTH_SHORT).show();
                    loadHolds();
                } else {
                    Toast.makeText(requireContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Hold>> call, Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onExpire(Hold hold) {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.confirm_expire_hold)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> submitExpire(hold))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void submitExpire(Hold hold) {
        apiService.expireHold(hold.id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), R.string.msg_hold_expired, Toast.LENGTH_SHORT).show();
                    loadHolds();
                } else {
                    Toast.makeText(requireContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
