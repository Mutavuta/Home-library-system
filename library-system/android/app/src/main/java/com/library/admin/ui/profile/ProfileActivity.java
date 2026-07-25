package com.library.admin.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.User;
import com.library.admin.ui.auth.LoginActivity;
import com.library.admin.utils.RetrofitClient;
import com.library.admin.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Shows the logged-in admin's own info and lets them log out.
// Reached from the profile icon button on the Users screen.
public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        sessionManager = new SessionManager(this);

        TextView tvFullName  = findViewById(R.id.tvFullName);
        TextView tvEmail     = findViewById(R.id.tvEmail);
        TextView tvRole      = findViewById(R.id.tvRole);
        TextView tvPhone     = findViewById(R.id.tvPhone);
        TextView tvCreatedAt = findViewById(R.id.tvCreatedAt);
        Button btnLogout     = findViewById(R.id.btnLogout);

        apiService.getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();

                    tvFullName.setText(user.fullName);
                    tvEmail.setText(user.email);
                    tvRole.setText(capitalize(user.role));
                    tvPhone.setText(user.phone);
                    tvCreatedAt.setText(user.createdAt);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v ->{
            sessionManager.clearSession();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // Clear the back stack so the admin can't press Back into the app after logging out
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

}
