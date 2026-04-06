package com.myandroid.nariguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SOSAlertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_sosalert);

        startSOS();

        findViewById(R.id.btnCancelSOS)
                .setOnClickListener(v -> stopSOS());
    }

    private void startSOS() {
        Intent intent = new Intent(this, LiveLocationService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    private void stopSOS() {
        stopService(new Intent(this, LiveLocationService.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        // Disable back press during SOS
        super.onBackPressed();
    }
}