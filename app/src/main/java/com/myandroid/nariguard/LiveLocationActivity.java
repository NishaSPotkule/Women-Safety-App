package com.myandroid.nariguard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LiveLocationActivity extends AppCompatActivity {

    private static final int LOCATION_REQ = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_location);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(v -> startSharing());
        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, LiveLocationService.class));
            Toast.makeText(this, "Live location stopped", Toast.LENGTH_SHORT).show();
        });
    }

    private void startSharing() {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    },
                    LOCATION_REQ
            );
            return;
        }

        Intent intent = new Intent(this, LiveLocationService.class);
        startForegroundService(intent);

        Toast.makeText(this,
                "Live location started",
                Toast.LENGTH_SHORT).show();
    }

    // 🔴 THIS WAS MISSING (MOST IMPORTANT)
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQ &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(this, LiveLocationService.class);
            startForegroundService(intent);

            Toast.makeText(this,
                    "Live location started",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
