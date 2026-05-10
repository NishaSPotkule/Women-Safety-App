package com.myandroid.nariguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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


        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Toast.makeText(SOSAlertActivity.this,
                                "Press STOP to cancel SOS",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startSOS() {
        Intent intent = new Intent(this, LiveLocationService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    private void stopSOS() {
        stopService(new Intent(this, LiveLocationService.class));
        finish();
    }
}