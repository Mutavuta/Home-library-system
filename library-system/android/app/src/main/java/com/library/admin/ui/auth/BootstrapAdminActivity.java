package com.library.admin.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.library.admin.R;
import com.library.admin.api.ApiService;
import com.library.admin.model.ApiResponse;
import com.library.admin.model.User;
import com.library.admin.utils.ApiErrorUtils;
import com.library.admin.utils.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// One-time setup screen - creates the very first admin account.
// The backend's bootstrap-admin endpoint self-locks after one admin exists,
// so this screen naturally becomes unusable once setup is complete.
public class BootstrapAdminActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etPhone, etEmail, etPassword, etConfirmPassword;
    private ProgressBar progressBar;
    private Button btnContinue;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bootstrap_admin);

        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        etFullName        = findViewById(R.id.etFullName);
        etPhone           = findViewById(R.id.etPhone);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        progressBar       = findViewById(R.id.progressBar);
        btnContinue       = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> attemptRegister());
    }

    // Validates the form, then calls the bootstrap-admin API
    private void attemptRegister() {
        String fullName = textOf(etFullName);
        String phone    = textOf(etPhone);
        String email    = textOf(etEmail);
        String password = textOf(etPassword);
        String confirm  = textOf(etConfirmPassword);

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(email)
        || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, R.string.error_password_short, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("fullName", fullName);
        body.put("phone", phone);
        body.put("email", email);
        body.put("password", password);

        setLoading(true);

        apiService.bootstrapAdmin(body).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call,
                                   Response<ApiResponse<User>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(BootstrapAdminActivity.this, R.string.msg_admin_created, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(BootstrapAdminActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(BootstrapAdminActivity.this, ApiErrorUtils.getErrorMassage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(BootstrapAdminActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String textOf(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnContinue.setEnabled(!loading);
    }

}
