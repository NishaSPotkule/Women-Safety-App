package com.myandroid.nariguard;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    TextView appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        appVersion = findViewById(R.id.appVersion);

        // Set app version dynamically
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            appVersion.setText("Version " + versionName);
        } catch (Exception e) {
            appVersion.setText("Version 1.0");
        }
    }
}
