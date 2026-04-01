package com.myandroid.nariguard;

import android.Manifest;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        locationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // UI Bindings
        TextView btnSos = view.findViewById(R.id.btnSos);
        LinearLayout shareLocationLayout = view.findViewById(R.id.shareLocationLayout);
        LinearLayout emergencyContacts = view.findViewById(R.id.emergencyContactLayout);
        LinearLayout safeLocationsLayout = view.findViewById(R.id.safeLocationsLayout);
        LinearLayout voiceRecordLayout = view.findViewById(R.id.voiceRecordLayout);
        LinearLayout safetyTipsLayout = view.findViewById(R.id.safetyTipsLayout);

        // Click Listeners
        btnSos.setOnClickListener(v -> startSOS());
        shareLocationLayout.setOnClickListener(v -> shareLocation());
        emergencyContacts.setOnClickListener(v -> startActivity(new Intent(getActivity(), EmergencycontactActivity.class)));
        safeLocationsLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), SafeLocationsActivity.class)));
        voiceRecordLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), VoiceRecordActivity.class)));
        safetyTipsLayout.setOnClickListener(v -> startActivity(new Intent(getActivity(), SafetyTipsActivity.class)));

        return view;
    }

    // ================= SHARE LOCATION (One-time) =================
    private void shareLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 202);
            return;
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
                    }
                });
    }

    // ================= SOS LOGIC (The 5-Minute Tracker) =================
    private void startSOS() {
        // 1. Check Fine Location Permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        // 2. Check Background Location Permission (For Android 11+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Please set location to 'Allow all the time' for continuous tracking", Toast.LENGTH_LONG).show();
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 103);
                return;
            }
        }

        // 3. Get current location to send immediate SMS
        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        sendSOSViaSmsApp(location);

                        // 4. Start the Live Service (5-minute updates)
                        Intent serviceIntent = new Intent(requireContext(), LiveLocationService.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            requireContext().startForegroundService(serviceIntent);
                        } else {
                            requireContext().startService(serviceIntent);
                        }

                        // 5. Open Alert Screen
                        startActivity(new Intent(requireContext(), SOSAlertActivity.class));
                    } else {
                        Toast.makeText(getContext(), "Error: Location not found. Enable GPS.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendSOSViaSmsApp(Location location) {
        if (auth.getUid() == null) return;

        firestore.collection("users").document(auth.getUid()).collection("emergency_contacts").get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) return;

                    StringBuilder numbers = new StringBuilder();
                    for (QueryDocumentSnapshot doc : query) {
                        String phone = doc.getString("phone");
                        if (phone != null) {
                            if (numbers.length() > 0) numbers.append(";");
                            numbers.append(phone);
                        }
                    }

                    String msg = "🚨 SOS ALERT!\n📍 My Live Location:\nhttps://maps.google.com/?q=" +
                            location.getLatitude() + "," + location.getLongitude();

                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(android.net.Uri.parse("smsto:" + numbers.toString()));
                    intent.putExtra("sms_body", msg);
                    startActivity(intent);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 101 || requestCode == 103) {
                startSOS(); // Restart the process once permission is granted
            } else if (requestCode == 202) {
                shareLocation();
            }
        } else {
            Toast.makeText(getContext(), "Permission required for SOS features", Toast.LENGTH_SHORT).show();
        }
    }
}