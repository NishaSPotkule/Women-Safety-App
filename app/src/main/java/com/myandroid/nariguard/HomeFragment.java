package com.myandroid.nariguard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    private FusedLocationProviderClient locationClient;
    private Location currentLocation;

    private static final int SOS_PERMISSION_CODE = 101;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        locationClient =
                LocationServices.getFusedLocationProviderClient(requireContext());

        fetchCurrentLocation();

        TextView btnSos = view.findViewById(R.id.btnSos);

        MaterialCardView shareLocationLayout =
                view.findViewById(R.id.shareLocationLayout);

        MaterialCardView emergencyContacts =
                view.findViewById(R.id.emergencyContactLayout);

        MaterialCardView safeLocationsLayout =
                view.findViewById(R.id.safeLocationsLayout);

        MaterialCardView voiceRecordLayout =
                view.findViewById(R.id.voiceRecordLayout);

        MaterialCardView safetyTipsLayout =
                view.findViewById(R.id.safetyTipsLayout);

        btnSos.setOnClickListener(v -> startSOS());

        shareLocationLayout.setOnClickListener(v -> shareLocation());

        emergencyContacts.setOnClickListener(v ->
                startActivity(new Intent(getActivity(),
                        EmergencycontactActivity.class)));

        voiceRecordLayout.setOnClickListener(v ->
                startActivity(new Intent(getActivity(),
                        VoiceRecordActivity.class)));

        safetyTipsLayout.setOnClickListener(v ->
                startActivity(new Intent(getActivity(),
                        SafetyTipsActivity.class)));

        safeLocationsLayout.setOnClickListener(v -> {

            if (currentLocation != null) {

                Intent intent =
                        new Intent(getActivity(),
                                SafeLocationsActivity.class);

                intent.putExtra("lat",
                        currentLocation.getLatitude());

                intent.putExtra("lon",
                        currentLocation.getLongitude());

                startActivity(intent);

            } else {

                Toast.makeText(getActivity(),
                        "Fetching location...",
                        Toast.LENGTH_SHORT).show();

                fetchCurrentLocation();
            }
        });

        return view;
    }

    private void fetchCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    300);

            return;
        }

        locationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null)
                .addOnSuccessListener(location -> {

                    if (location != null) {
                        currentLocation = location;
                    }
                });
    }

    private void shareLocation() {

        Context context = getContext();

        if (context == null) return;

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    202);

            return;
        }

        CancellationTokenSource cts =
                new CancellationTokenSource();

        locationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.getToken())
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        String link =
                                "https://maps.google.com/?q="
                                        + location.getLatitude()
                                        + ","
                                        + location.getLongitude();

                        Intent intent =
                                new Intent(Intent.ACTION_SEND);

                        intent.setType("text/plain");

                        intent.putExtra(
                                Intent.EXTRA_TEXT,
                                "📍 My Current Location:\n" + link);

                        startActivity(
                                Intent.createChooser(
                                        intent,
                                        "Share via"));

                    } else {

                        Toast.makeText(context,
                                "Location is null",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startSOS() {

        Context context = requireContext();

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    SOS_PERMISSION_CODE);

            return;
        }

        locationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null)
                .addOnSuccessListener(location -> {

                    if (location == null) {

                        Toast.makeText(context,
                                "Enable GPS",
                                Toast.LENGTH_SHORT).show();

                        return;
                    }

                    sendSOSMessage(location);

                    Intent serviceIntent =
                            new Intent(context,
                                    LiveLocationService.class);

                    ContextCompat.startForegroundService(
                            context,
                            serviceIntent);
                });
    }

    private void sendSOSMessage(Location location) {

        String message =
                "SOS Help Needed. Location: https://maps.google.com/?q="
                        + location.getLatitude()
                        + ","
                        + location.getLongitude();

        try {

            Intent intent =
                    new Intent(Intent.ACTION_VIEW);

            intent.setData(Uri.parse("sms:"));

            intent.putExtra("sms_body", message);

            startActivity(intent);

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(getContext(),
                    "SMS app not found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (grantResults.length == 0) return;

        if (requestCode == SOS_PERMISSION_CODE) {

            if (grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                startSOS();

            } else {

                Toast.makeText(getContext(),
                        "Location permission required",
                        Toast.LENGTH_LONG).show();
            }
        }

        else if (requestCode == 202) {

            shareLocation();
        }

        else if (requestCode == 300) {

            fetchCurrentLocation();
        }
    }
}