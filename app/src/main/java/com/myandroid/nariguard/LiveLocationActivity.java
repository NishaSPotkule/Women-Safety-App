package com.myandroid.nariguard;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LiveLocationActivity extends AppCompatActivity {

    private static final int REQ = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_location);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(v -> startServiceSafe());
        btnStop.setOnClickListener(v -> stopServiceSafe());
    }

    private void startServiceSafe() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ);
            return;
        }

        Intent intent = new Intent(this, LiveLocationService.class);
        ContextCompat.startForegroundService(this, intent);

        Toast.makeText(this, "Live location started", Toast.LENGTH_SHORT).show();
    }

    private void stopServiceSafe() {
        stopService(new Intent(this, LiveLocationService.class));
        Toast.makeText(this, "Live location stopped", Toast.LENGTH_SHORT).show();
    }
}