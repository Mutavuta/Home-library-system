package com.library.admin.ui.books;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.BookTitle;
import com.library.admin.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Books screen - shows the full catalog, supports live search by title/author,
// and lets admin scan a barcode to add a new physical copy.
public class BooksFragment extends Fragment {

    private RecyclerView recyclerBooks;
    private SwipeRefreshLayout swipeRefresh;
    private EditText etSearch;
    private MaterialButton btnScanBarcode;

    private BookTitleAdapter adapter;
    private List<BookTitle> allTitles = new ArrayList<>();
    private ApiService apiService;

    // Modern replacement for startActivityForResult - launches the barcode scanner
    // and receives the scanned result back in the callback below
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    onBarcodeScanned(result.getContents());
                }
            });

    // Handles the runtime camera permission request required before scanning
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
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        recyclerBooks  = view.findViewById(R.id.recyclerBooks);
        swipeRefresh   = view.findViewById(R.id.swipeRefresh);
        etSearch       = view.findViewById(R.id.etSearch);
        btnScanBarcode = view.findViewById(R.id.btnScanBarcode);

        adapter = new BookTitleAdapter();
        recyclerBooks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerBooks.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadTitles);
        btnScanBarcode.setOnClickListener(v -> checkCameraPermissionAndScan());

        setupSearch();
        loadTitles();

        return view;
    }

    // Live filters the already-loaded list as the admin types - no extra API calls needed
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTitles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filterTitles(String query) {
        if (query.isEmpty()) {
            adapter.setTitles(allTitles);
            return;
        }
        String lower = query.toLowerCase();
        List<BookTitle> filtered = new ArrayList<>();
        for (BookTitle t : allTitles) {
            if (t.title.toLowerCase().contains(lower) || t.author.toLowerCase().contains(lower)) {
                filtered.add(t);
            }
        }
        adapter.setTitles(filtered);
    }

    private void loadTitles() {
        apiService.getAllTitles().enqueue(new Callback<ApiResponse<List<BookTitle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookTitle>>> call,
                                   Response<ApiResponse<List<BookTitle>>> response) {
                swipeRefresh.setRefreshing(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    allTitles = response.body().getData();
                    adapter.setTitles(allTitles);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookTitle>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan the book's barcode");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void onBarcodeScanned(String barcode) {
        AddBookDialog dialog = new AddBookDialog(requireContext(), barcode, allTitles);
        dialog.setOnBookAddedListener(this::loadTitles);
        dialog.show();
    }
}
