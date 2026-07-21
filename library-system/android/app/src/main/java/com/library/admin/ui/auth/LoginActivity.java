package com.library.admin.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.library.admin.MainActivity;
import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.AuthRequest;
import com.library.admin.model.AuthResponse;
import com.library.admin.utils.ApiErrorUtils;
import com.library.admin.utils.RetrofitClient;
import com.library.admin.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Entry point of the app - checks for an existing session first, and
// otherwise lets an admin log in. Only role="admin" accounts are permitted
// here; this app never serves borrower accounts.
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private ProgressBar progressBar;
    private Button btnLogin;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Already logged in as an approved admin - skip straight to the dashboard
        if (sessionManager.isLoggedIn()
        && "admin".equals(sessionManager.getRole())
        && "approved".equals(sessionManager.getStatus())) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);
        btnLogin    = findViewById(R.id.btnLogin);
        TextView tvSetupAdmin = findViewById(R.id.tvSetupAdmin);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSetupAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, BootstrapAdminActvity.class)));
    }

    // Validates the form, then calls the login API
    private void attemptLogin() {
        String email    = etEmail.getText()    != null ? etEmail.getText().toString().trim()    : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        apiService.login(new AuthRequest(email, password))
                .enqueue(new Callback<ApiResponse<AuthResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthResponse>> call,
                                           Response<ApiResponse<AuthResponse>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AuthResponse auth = response.body().getData();

                            // Admin-only app - reject borrower accounts even with valid credentials
                            if (!"admin".equals(auth.role)) {
                                Toast.makeText(LoginActivity.this, R.string.error_admin_only, Toast.LENGTH_LONG).show();
                                return;
                            }

                            if ("suspended".equals(auth.status)) {
                                Toast.makeText(LoginActivity.this, R.string.error_account_suspended, Toast.LENGTH_LONG).show();
                            }

                            sessionManager.saveSession(auth.token, auth.userId, auth.fullName, auth.role, auth.status);

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Shows/hides the progress bar and disables the button while a request is in flight
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

}
