package com.myandroid.nariguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    LinearLayout layoutProfile, layoutEmergency, layoutAbout, layoutLogout;
    Switch switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        layoutProfile = view.findViewById(R.id.layoutProfile);
        layoutEmergency = view.findViewById(R.id.layoutEmergency);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutLogout = view.findViewById(R.id.layoutLogout);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);


        switchDarkMode.setOnCheckedChangeListener(null);
        int nightMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(nightMode == AppCompatDelegate.MODE_NIGHT_YES);


        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

            int newMode = isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;

            if (AppCompatDelegate.getDefaultNightMode() != newMode) {

                AppCompatDelegate.setDefaultNightMode(newMode);

                // ✅ SAFEST WAY
                if (getActivity() != null) {
                    getActivity().recreate();  // 🔥 no crash, clean restart
                }
            }
        });


        layoutProfile.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ProfileActivity.class))
        );

        // ---- EMERGENCY ----
        layoutEmergency.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EmergencycontactActivity.class))
        );

        // ---- ABOUT ----
        layoutAbout.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AboutActivity.class))
        );

        // ---- LOGOUT ----
        layoutLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
