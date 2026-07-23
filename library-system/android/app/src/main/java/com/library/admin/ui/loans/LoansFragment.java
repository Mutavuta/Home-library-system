package com.library.admin.ui.loans;

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
import com.library.admin.model.BookTitle;
import com.library.admin.model.Loan;
import com.library.admin.model.User;
import com.library.admin.utils.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Loans screen - Active/Overdue tabs, plus Confirm Loan (Select Hold -> Confirm
// Loan popups) and Scan to Return (scan -> Process Return -> Result popups).
public class LoansFragment extends Fragment {

    private Button btnConfirmLoan, btnScanToReturn, tabActive, tabOverdue;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerLoans;

    private LoanAdapter adapter;
    private ApiService apiService;
    private boolean showingOverdue = false;

    private final ActivityResultLauncher<ScanOptions> returnScanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    openProcessReturnDialog(result.getContents());
                }
            });

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchReturnScanner();
                } else {
                    Toast.makeText(requireContext(), R.string.error_camera_permission, Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loans, container, false);

        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        btnConfirmLoan  = view.findViewById(R.id.btnConfirmLoan);
        btnScanToReturn = view.findViewById(R.id.btnScanToReturn);
        tabActive       = view.findViewById(R.id.tabActive);
        tabOverdue      = view.findViewById(R.id.tabOverdue);
        swipeRefresh    = view.findViewById(R.id.swipeRefresh);
        recyclerLoans   = view.findViewById(R.id.recyclerLoans);

        adapter = new LoanAdapter();
        recyclerLoans.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLoans.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadLoans);

        tabActive.setOnClickListener(v -> {
            showingOverdue = false;
            setActiveTab(tabActive, tabOverdue);
            loadLoans();
        });
        tabOverdue.setOnClickListener(v -> {
            showingOverdue = true;
            setActiveTab(tabOverdue, tabActive);
            loadLoans();
        });

        btnConfirmLoan.setOnClickListener(v -> openSelectHoldDialog());
        btnScanToReturn.setOnClickListener(v -> checkCameraPermissionAndScan());

        loadLoans();

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

    // Loads either active or overdue loans depending on the current tab, then
    // fetches users and titles once to attach readable names locally
    private void loadLoans() {
        Call<ApiResponse<List<Loan>>> call = showingOverdue
                ? apiService.getOverdueLoans()
                : apiService.getActiveLoans();

        call.enqueue(new Callback<ApiResponse<List<Loan>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Loan>>> call, Response<ApiResponse<List<Loan>>> response) {
                swipeRefresh.setRefreshing(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Loan> loans = response.body().getData();
                    enrichLoans(loans);
                } else {
                    Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Loan>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (isAdded()) Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enrichLoans(List<Loan> loans) {
        apiService.getAllUsers().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (!isAdded()) return;

                Map<String, String> nameMap = new HashMap<>();
                Map<String, String> emailMap = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    for (User u : response.body().getData()) {
                        nameMap.put(u.id, u.fullName);
                        emailMap.put(u.id, u.email);
                    }
                }
                for (Loan l : loans) {
                    l.borrowerName  = nameMap.get(l.userId);
                    l.borrowerEmail = emailMap.get(l.userId);
                }
                attachBookTitles(loans);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                attachBookTitles(loans);
            }
        });
    }

    private void attachBookTitles(List<Loan> loans) {
        apiService.getTitlesForStats().enqueue(new Callback<ApiResponse<List<BookTitle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookTitle>>> call, Response<ApiResponse<List<BookTitle>>> response) {
                if (!isAdded()) return;

                Map<String, String> titleMap = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    for (BookTitle t : response.body().getData()) {
                        titleMap.put(t.id, t.title);
                    }
                }
                for (Loan l : loans) {
                    l.bookTitle = titleMap.get(l.titleId);
                }
                adapter.setLoans(loans);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookTitle>>> call, Throwable t) {
                adapter.setLoans(loans);
            }
        });
    }

    private void openSelectHoldDialog() {
        SelectHoldDialog dialog = new SelectHoldDialog(requireContext());
        dialog.setOnHoldSelectedListener(hold -> {
            ConfirmLoanDialog confirmDialog =
                    new ConfirmLoanDialog(requireContext(), hold, getParentFragmentManager());
            confirmDialog.setOnLoanConfirmedListener(this::loadLoans);
            confirmDialog.show();
        });
        dialog.show();
    }

    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchReturnScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchReturnScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan the barcode of the book being returned");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        returnScanLauncher.launch(options);
    }

    private void openProcessReturnDialog(String barcode) {
        ProcessReturnDialog dialog = new ProcessReturnDialog(requireContext(), barcode, getParentFragmentManager());
        dialog.setOnReturnedListener((loan, book) -> {
            loadLoans();
            String borrowerName = "the borrower"; // ProcessReturnDialog already resolved and displayed the name;
            // Loan object alone doesn't carry it back here
            new ReturnResultDialog(requireContext(), book, borrowerName).show();
        });
        dialog.show();
    }
}
