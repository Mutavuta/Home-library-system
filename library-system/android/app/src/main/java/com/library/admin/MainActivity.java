package com.library.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.library.admin.ui.auth.LoginActivity;
import com.library.admin.ui.books.BooksFragment;
import com.library.admin.ui.dashboard.DashboardFragment;
import com.library.admin.ui.holds.HoldsFragment;
import com.library.admin.ui.loans.LoansFragment;
import com.library.admin.ui.users.UsersFragment;
import com.library.admin.utils.SessionManager;

// The app shell - hosts one fragment at a time in fragmentContainer and
// a custom floating bottom nav (plain LinearLayouts, not BottomNavigationView,
// since that component rendered inconsistently across devices)
public class MainActivity extends AppCompatActivity {

    // the five item containers, each holding one icon + one label
    private LinearLayout navItemHome, navItemBooks, navItemHolds, navItemLoans, navItemUsers;
    private ImageView ivHome, ivBooks, ivHolds, ivLoans, ivUsers;
    private TextView tvNavHome, tvNavBooks, tvNavHolds, tvNavLoans, tvNavUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Guard: if the session was cleared (e.g. logout from another screen)
        // bounce back to Login before this screen even renders
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        setContentView(R.layout.activity_main);

        bindViews();
        setupNavClicks();

        // Dashboard is the default screen shown on launch
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            setActiveTab(navItemHome, ivHome, tvNavHome);
        }
    }

    private void bindViews() {
        navItemHome  = findViewById(R.id.navItemHome);
        navItemBooks = findViewById(R.id.navItemBooks);
        navItemHolds = findViewById(R.id.navItemHolds);
        navItemLoans = findViewById(R.id.navItemLoans);
        navItemUsers = findViewById(R.id.navItemUsers);

        ivHome  = findViewById(R.id.ivHome);
        ivBooks = findViewById(R.id.ivBooks);
        ivHolds = findViewById(R.id.ivHolds);
        ivLoans = findViewById(R.id.ivLoans);
        ivUsers = findViewById(R.id.ivUsers);

        tvNavHome  = findViewById(R.id.tvNavHome);
        tvNavBooks = findViewById(R.id.tvNavBooks);
        tvNavHolds = findViewById(R.id.tvNavHolds);
        tvNavLoans = findViewById(R.id.tvNavLoans);
        tvNavUsers = findViewById(R.id.tvNavUsers);
    }

    private void setupNavClicks() {
        navItemHome.setOnClickListener(v -> {
            loadFragment(new DashboardFragment());
            setActiveTab(navItemHome, ivHome, tvNavHome);
        });

        navItemBooks.setOnClickListener(v -> {
            loadFragment(new BooksFragment());
            setActiveTab(navItemBooks, ivBooks, tvNavBooks);
        });

        navItemHolds.setOnClickListener(v -> {
            loadFragment(new HoldsFragment());
            setActiveTab(navItemHolds, ivHolds, tvNavHolds);
        });

        navItemLoans.setOnClickListener(v -> {
            loadFragment(new LoansFragment());
            setActiveTab(navItemLoans, ivLoans, tvNavLoans);
        });

        navItemUsers.setOnClickListener(v -> {
            loadFragment(new UsersFragment());
            setActiveTab(navItemUsers, ivUsers, tvNavUsers);
        });
    }

    // Swaps the fragment shown in fragmentContainer
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    // Resets all five nav items to inactive color, then highlights only the selected one
    private void setActiveTab(LinearLayout selectedItem, ImageView selectedIcon, TextView selectedLabel) {
        int activeColor   = getResources().getColor(R.color.white, getTheme());
        int inactiveColor = getResources().getColor(R.color.black, getTheme());

        resetTab(ivHome, tvNavHome, inactiveColor);
        resetTab(ivBooks, tvNavBooks, inactiveColor);
        resetTab(ivHolds, tvNavHolds, inactiveColor);
        resetTab(ivLoans, tvNavLoans, inactiveColor);
        resetTab(ivUsers, tvNavUsers, inactiveColor);

        selectedIcon.setColorFilter(activeColor);
        selectedLabel.setTextColor(activeColor);
    }

    private void resetTab(ImageView icon, TextView label, int color){
        icon.setColorFilter(color);
        label.setTextColor(color);
    }
}