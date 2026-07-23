package com.library.admin.ui.users;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.User;
import com.library.admin.ui.profile.ProfileActivity;
import com.library.admin.utils.ApiErrorUtils;
import com.library.admin.utils.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Users screen - Pending Approval/All Users tabs, plus the admin profile
// button in the header. Each row's buttons change based on the user's status.
public class UsersFragment extends Fragment implements UserAdapter.OnUserActionListener {

    private Button tabPending, tabAll;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerUsers;

    private UserAdapter adapter;
    private ApiService apiService;
    private boolean showingPendingOnly = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        apiService = RetrofitClient.getInstance(requireContext()).create(ApiService.class);

        ImageButton btnAdminProfile = view.findViewById(R.id.btnAdminProfile);
        btnAdminProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileActivity.class)));

        tabPending    = view.findViewById(R.id.tabPending);
        tabAll        = view.findViewById(R.id.tabAll);
        swipeRefresh  = view.findViewById(R.id.swipeRefresh);
        recyclerUsers = view.findViewById(R.id.recyclerUsers);

        adapter = new UserAdapter(this);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUsers.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadUsers);

        tabPending.setOnClickListener(v -> {
            showingPendingOnly = true;
            setActiveTab(tabPending, tabAll);
            loadUsers();
        });
        tabAll.setOnClickListener(v -> {
            showingPendingOnly = false;
            setActiveTab(tabAll, tabPending);
            loadUsers();
        });

        loadUsers();

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

    private void loadUsers() {
        Call<ApiResponse<List<User>>> call = showingPendingOnly
                ? apiService.getPendingUsers()
                : apiService.getAllUsers();

        call.enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                swipeRefresh.setRefreshing(false);
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    adapter.setUsers(response.body().getData());
                } else {
                    Toast.makeText(requireContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (isAdded()) Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(User user) {
        apiService.approveUser(user.id).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), R.string.msg_user_approved, Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(requireContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSuspend(User user) {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.confirm_suspend_user)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> submitSuspend(user))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void submitSuspend(User user) {
        apiService.suspendUser(user.id).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), R.string.msg_user_suspended, Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(requireContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReactivate(User user) {
        apiService.reactivateUser(user.id).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), R.string.msg_user_reactivated, Toast.LENGTH_SHORT).show();
                    loadUsers();
                } else {
                    Toast.makeText(requireContext(), ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDelete(User user) {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.confirm_delete_user)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> submitDelete(user))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void submitDelete(User user) {
        apiService.deleteUser(user.id).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), R.string.msg_user_deleted, Toast.LENGTH_SHORT).show();
                    loadUsers();
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
