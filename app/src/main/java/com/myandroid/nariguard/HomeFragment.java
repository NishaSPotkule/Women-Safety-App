package com.myandroid.nariguard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HomeFragment extends Fragment {

    private FusedLocationProviderClient locationClient;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private Location currentLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        fetchCurrentLocation();

        TextView btnSos = view.findViewById(R.id.btnSos);
        LinearLayout shareLocationLayout = view.findViewById(R.id.shareLocationLayout);
        LinearLayout emergencyContacts = view.findViewById(R.id.emergencyContactLayout);
        LinearLayout safeLocationsLayout = view.findViewById(R.id.safeLocationsLayout);
        LinearLayout voiceRecordLayout = view.findViewById(R.id.voiceRecordLayout);
        LinearLayout safetyTipsLayout = view.findViewById(R.id.safetyTipsLayout);

        btnSos.setOnClickListener(v -> startSOS());
        shareLocationLayout.setOnClickListener(v -> shareLocation());
        emergencyContacts.setOnClickListener(v -> startActivity(new Intent(getActivity(), EmergencycontactActivity.class)));

        voiceRecordLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), VoiceRecordActivity.class)));
        safetyTipsLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), SafetyTipsActivity.class)));

        // ✅ FIXED SafeLocations click
        safeLocationsLayout.setOnClickListener(v -> {

            if (currentLocation != null) {

                Intent intent = new Intent(getActivity(), SafeLocationsActivity.class);
                intent.putExtra("lat", currentLocation.getLatitude());
                intent.putExtra("lon", currentLocation.getLongitude());

                startActivity(intent);

            } else {
                Toast.makeText(getActivity(), "Fetching location... Please wait", Toast.LENGTH_SHORT).show();
                fetchCurrentLocation(); // retry
            }
        });

        return view;
    }

    // ✅ NEW METHOD (only addition)
    private void fetchCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 300);
            return;
        }

        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                    }
                });
    }

    private void shareLocation() {
        Context context = getContext();
        if (context == null) return;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 202);
            return;
        }

        if (locationClient == null) {
            locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        }

        CancellationTokenSource cts = new CancellationTokenSource();

        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String link = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, "📍 My Current Location:\n" + link);

                        startActivity(Intent.createChooser(intent, "Share via"));
                    } else {
                        Toast.makeText(context, "Location is null", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void startSOS() {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Please set location to 'Allow all the time' for continuous tracking", Toast.LENGTH_LONG).show();
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 103);
                return;
            }
        }

        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        sendSOSViaSmsApp(location);

                        new android.os.Handler().postDelayed(() -> {
                            startActivity(new Intent(requireContext(), SOSAlertActivity.class));
                        }, 2000);
                    } else {
                        Toast.makeText(getContext(), "Error: Location not found. Enable GPS.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendSOSViaSmsApp(Location location) {

        if (auth.getUid() == null) return;

        firestore.collection("users")
                .document(auth.getUid())
                .collection("emergency_contacts")
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(getContext(), "No emergency contacts found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String firstNumber = null;

                    for (QueryDocumentSnapshot doc : query) {
                        String phone = doc.getString("phone");

                        if (phone != null) {
                            phone = phone.replaceAll("\\s+", "");
                            if (!phone.isEmpty()) {
                                firstNumber = phone;
                                break;
                            }
                        }
                    }

                    if (firstNumber == null) {
                        Toast.makeText(getContext(), "No valid contact found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String msg = "🚨 SOS ALERT!\n📍 My Live Location:\nhttps://maps.google.com/?q="
                            + location.getLatitude() + "," + location.getLongitude();

                    try {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(android.net.Uri.parse("smsto:" + firstNumber));
                        intent.putExtra("sms_body", msg);

                        startActivity(intent);

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Unable to open SMS app", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load contacts", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == 101 || requestCode == 103) {
                startSOS();
            } else if (requestCode == 202) {
                shareLocation();
            }
            // ✅ NEW CASE (SafeLocations)
            else if (requestCode == 300) {
                fetchCurrentLocation();
            }

        } else {
            Toast.makeText(getContext(), "Permission required for SOS features", Toast.LENGTH_SHORT).show();
        }
    }
}