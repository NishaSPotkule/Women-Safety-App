package com.myandroid.nariguard;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LiveLocationService extends Service {

    private static final String CHANNEL_ID = "SOS_LIVE_LOCATION";
    private static final int NOTIFICATION_ID = 1;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotificationChannel();

        startForeground(NOTIFICATION_ID, getNotification());


        startLocationUpdates();
    }

    private void startLocationUpdates() {

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000
        )
                .setMinUpdateIntervalMillis(3000)
                .setWaitForAccurateLocation(true)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location location = result.getLastLocation();
                if (location != null) {
                    updateFirebase(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } else {
            stopSelf();
        }
    }

    private void updateFirebase(Location location) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) uid = "anonymous";

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("SOS_Live")
                .child(uid);

        ref.setValue(new LocationModel(
                location.getLatitude(),
                location.getLongitude(),
                System.currentTimeMillis()
        ));
    }

    private Notification getNotification() {

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NariGuard SOS Active")
                .setContentText("Sharing live location with emergency contacts...")
                .setSmallIcon(R.drawable.location) // Ensure this icon exists
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SOS Live Location",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationClient != null && locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}