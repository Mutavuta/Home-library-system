package com.library.admin.ui.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.library.admin.R;
import com.library.admin.ui.profile.ProfileActivity;

// Shows all users (pending/all tabs) and gives access to the admin's own profile.
public class UsersFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                         @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        ImageButton btnAdminProfile = view.findViewById(R.id.btnAdminProfile);
        btnAdminProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileActivity.class)));
        return view;
    }

}
