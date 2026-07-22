package com.library.admin.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.library.admin.R;
import com.library.admin.ui.auth.LoginActivity;
import com.library.admin.utils.SessionManager;

// Shows the logged-in admin's own info and lets them log out.
// Reached from the profile icon button on the Users screen.
public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        TextView tvFullName  = findViewById(R.id.tvFullName);
        TextView tvEmail     = findViewById(R.id.tvEmail);
        TextView tvRole      = findViewById(R.id.tvRole);
        TextView tvPhone     = findViewById(R.id.tvPhone);
        TextView tvCreatedAt = findViewById(R.id.tvCreatedAt);
        Button btnLogout     = findViewById(R.id.btnLogout);

        // SessionManager only stores what came back from the login response -
        // fullName, role and status. Email/phone/createdAt aren't part of that
        // response, so those two fields stay blank here until the app fetches
        // the full user profile from GET /users/me. Marking this as a known gap.
        tvFullName.setText(sessionManager.getFullName());
        tvRole.setText(capitalize(sessionManager.getRole()));
        tvEmail.setText("");
        tvPhone.setText("");
        tvCreatedAt.setText("");

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
