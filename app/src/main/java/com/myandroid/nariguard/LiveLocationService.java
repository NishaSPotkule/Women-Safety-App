package com.myandroid.nariguard;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LiveLocationService extends Service {

    private FusedLocationProviderClient client;
    private LocationCallback callback;

    @Override
    public void onCreate() {
        super.onCreate();

        client = LocationServices.getFusedLocationProviderClient(this);

        startForeground(1, getNotification());
        startUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void startUpdates() {

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                15 * 60 * 1000).build();

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {

                Location loc = result.getLastLocation();
                if (loc != null) updateFirebase(loc);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            client.requestLocationUpdates(request, callback, Looper.getMainLooper());
        }
    }

    private void updateFirebase(Location loc) {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseDatabase.getInstance()
                .getReference("SOS_Live")
                .child(uid)
                .push()
                .setValue(new LocationModel(
                        loc.getLatitude(),
                        loc.getLongitude(),
                        System.currentTimeMillis()
                ));
    }

    private Notification getNotification() {

        String channelId = "SOS_CHANNEL";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "SOS Service",
                    NotificationManager.IMPORTANCE_HIGH
            );

            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("🚨 SOS Active")
                .setContentText("Sharing live location...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (client != null && callback != null) {
            client.removeLocationUpdates(callback);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}