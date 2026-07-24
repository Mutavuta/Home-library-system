package com.library.admin.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.Book;
import com.library.admin.model.BookTitle;
import com.library.admin.model.Loan;
import com.library.admin.model.User;
import com.library.admin.utils.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// The Overview/Dashboard screen - shows 8 aggregate stat cards.
// Makes 4 separate API calls in parallel since the backend has no single
// combined stats endpoint; each response updates its own set of cards.
public class DashboardFragment extends Fragment {

    private TextView tvDate, tvBookTitles, tvAvailable, tvReserved, tvOnLoan,
            tvOverdueLoans, tvPendingUsers, tvAllBooks, tvAllUsers;
    private SwipeRefreshLayout swipeRefresh;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        tvDate         = view.findViewById(R.id.tvDate);
        tvBookTitles   = view.findViewById(R.id.tvBookTitles);
        tvAvailable    = view.findViewById(R.id.tvAvailable);
        tvReserved     = view.findViewById(R.id.tvReserved);
        tvOnLoan       = view.findViewById(R.id.tvOnLoan);
        tvOverdueLoans = view.findViewById(R.id.tvOverdueLoans);
        tvPendingUsers = view.findViewById(R.id.tvPendingUsers);
        tvAllBooks     = view.findViewById(R.id.tvAllBooks);
        tvAllUsers     = view.findViewById(R.id.tvAllUsers);

        // Note: fragment_overview.xml currently has no SwipeRefreshLayout wrapper,
        // so pull-to-refresh isn't available yet on this screen - loads once on open
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date()));

        loadStats();

        return view;
    }

    // Fires all four stat-fetching calls - each one only updates the cards it owns
    private void loadStats() {
        loadTitleStats();
        loadBookStats();
        loadOverdueStats();
        loadUserStats();
    }

    // Book Titles, Available, Reserved, On loan - summed across every title
    private void loadTitleStats() {
        apiService.getTitlesForStats().enqueue(new Callback<ApiResponse<List<BookTitle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookTitle>>> call,
                                   Response<ApiResponse<List<BookTitle>>> response) {
                if (!isAdded()) return; // fragment may have been detached before response arrives
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<BookTitle> titles = response.body().getData();
                    int available = 0, reserved = 0, onLoan = 0;
                    for (BookTitle t : titles) {
                        available += t.availableCopies;
                        reserved  += t.reservedCopies;
                        onLoan    += t.loanedCopies;
                    }

                    tvBookTitles.setText(String.valueOf(titles.size()));
                    tvAvailable.setText(String.valueOf(available));
                    tvReserved.setText(String.valueOf(reserved));
                    tvOnLoan.setText(String.valueOf(onLoan));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookTitle>>> call, Throwable t) {
                // Silently leave the cards at "0" - a failed background stat refresh
                // shouldn't interrupt the admin with an error toast
            }
        });
    }

    // All Books count - total physical copies regardless of status
    private void loadBookStats() {
        apiService.getAllBooksForStats().enqueue(new Callback<ApiResponse<List<Book>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Book>>> call,
                                   Response<ApiResponse<List<Book>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvAllBooks.setText(String.valueOf(response.body().getData().size()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Book>>> call, Throwable t) { }
        });
    }

    // Overdue Loans count
    private void loadOverdueStats() {
        apiService.getOverdueLoansForStats().enqueue(new Callback<ApiResponse<List<Loan>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Loan>>> call,
                                   Response<ApiResponse<List<Loan>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvOverdueLoans.setText(String.valueOf(response.body().getData().size()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Loan>>> call, Throwable t) { }
        });
    }

    // Pending Users and All Users counts
    private void loadUserStats() {
        apiService.getPendingUsersForStats().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call,
                                   Response<ApiResponse<List<User>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvPendingUsers.setText(String.valueOf(response.body().getData().size()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) { }
        });

        apiService.getAllUsersForStats().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call,
                                   Response<ApiResponse<List<User>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    tvAllUsers.setText(String.valueOf(response.body().getData().size()));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) { }
        });
    }

}
