package com.myandroid.nariguard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    MaterialToolbar toolbar;

    FirebaseAuth auth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        SharedPreferences prefs =
                getSharedPreferences("settings", MODE_PRIVATE);

        boolean darkMode = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                darkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }


        loadUserName(user.getUid());


        checkDefaultSmsApp();


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);


        loadUserName(user.getUid());
    }


    private void loadUserName(String uid) {

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    String userName = documentSnapshot.getString("name");

                    if (userName == null || userName.isEmpty()) {
                        userName = "User";
                    }

                    // Set toolbar title
                    toolbar.setTitle("Welcome, " + userName + " 👋");

                    // Setup navigation AFTER name loads
                    setupBottomNavigation(userName);
                })
                .addOnFailureListener(e -> {
                    toolbar.setTitle("Welcome 👋");
                    setupBottomNavigation("User");
                });
    }


    private void setupBottomNavigation(String userName) {


        loadFragment(new HomeFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment selected = null;

            if (item.getItemId() == R.id.nav_home) {
                selected = new HomeFragment();
                toolbar.setTitle("Welcome, " + userName + " 👋");

            } else if (item.getItemId() == R.id.nav_map) {
                selected = new MapFragment();
                toolbar.setTitle("Map");

            } else if (item.getItemId() == R.id.nav_menu) {
                selected = new SettingsFragment();
                toolbar.setTitle("Settings");
            }

            if (selected != null) {
                loadFragment(selected);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void checkDefaultSmsApp() {

        if (android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.KITKAT) {

            String defaultSmsPackage =
                    Telephony.Sms.getDefaultSmsPackage(this);

            String myPackage = getPackageName();

            if (defaultSmsPackage == null ||
                    !defaultSmsPackage.equals(myPackage)) {

                Intent intent =
                        new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);

                intent.putExtra(
                        Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        myPackage
                );

                SharedPreferences prefs = getSharedPreferences("app", MODE_PRIVATE);
                boolean asked = prefs.getBoolean("sms_default_asked", false);

                if (!asked) {

                    startActivity(intent);

                    prefs.edit().putBoolean("sms_default_asked", true).apply();
                }
            }
        }
    }
}