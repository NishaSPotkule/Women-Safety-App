package com.myandroid.nariguard;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

public class SafeLocationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SafeLocationAdapter adapter;
    private ArrayList<SafeLocation> safeLocations;
    private ProgressBar progressBar;

    private double userLat;
    private double userLon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_locations);

        recyclerView = findViewById(R.id.rvSafeLocations);
        progressBar=findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        userLat = getIntent().getDoubleExtra("lat", -1);
        userLon = getIntent().getDoubleExtra("lon", -1);

        Log.d("LOCATION_DEBUG", "Lat: " + userLat + " Lon: " + userLon);


        if (userLat == -1 || userLon == -1) {
            Toast.makeText(this, "Location unavailable. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        safeLocations = new ArrayList<>();

        adapter = new SafeLocationAdapter(safeLocations, location ->
                Toast.makeText(this,
                        location.name + " selected",
                        Toast.LENGTH_SHORT).show());

        recyclerView.setAdapter(adapter);

        fetchNearbySafeLocations(userLat, userLon);
    }

    private void fetchNearbySafeLocations(double lat, double lon) {

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {

            int radius = 3000;
            boolean foundAny = false;
            ArrayList<SafeLocation> tempList = new ArrayList<>();

            while (!foundAny && radius <= 20000) {

                try {
                    String query = "[out:json];(" +
                            "node[amenity=police](around:" + radius + "," + lat + "," + lon + ");" +
                            "node[amenity=hospital](around:" + radius + "," + lat + "," + lon + ");" +
                            ");out;";

                    String urlStr = "https://overpass-api.de/api/interpreter?data=" +
                            URLEncoder.encode(query, "UTF-8");

                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);

                    int responseCode = conn.getResponseCode();

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw new Exception("API Error: " + responseCode);
                    }

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));

                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();

                    Log.d("SafeLocationAPI", response.toString());

                    JSONObject json = new JSONObject(response.toString());
                    JSONArray elements = json.getJSONArray("elements");

                    tempList.clear();

                    for (int i = 0; i < elements.length(); i++) {

                        JSONObject obj = elements.getJSONObject(i);

                        double placeLat = obj.getDouble("lat");
                        double placeLon = obj.getDouble("lon");

                        JSONObject tags = obj.optJSONObject("tags");
                        if (tags == null) continue;

                        String type = tags.optString("amenity", "unknown");
                        String name = tags.optString("name", "");

                        // ✅ Better default names
                        if (name.isEmpty()) {
                            name = type.equals("police")
                                    ? "Police Station"
                                    : "Hospital";
                        }

                        float[] result = new float[1];
                        Location.distanceBetween(
                                lat, lon,
                                placeLat, placeLon,
                                result
                        );

                        double distanceKm = result[0] / 1000.0;

                        tempList.add(new SafeLocation(
                                name,
                                placeLat,
                                placeLon,
                                distanceKm,
                                type
                        ));
                    }

                    if (elements.length() > 0)
                        foundAny = true;
                    else
                        radius *= 2;

                } catch (Exception e) {

                    e.printStackTrace();

                    runOnUiThread(() ->
                            Toast.makeText(this,
                                    "Failed to fetch safe locations",
                                    Toast.LENGTH_SHORT).show());

                    return;
                }
            }


            Collections.sort(tempList,
                    (a, b) -> Double.compare(a.distance, b.distance));

            runOnUiThread(() -> {

                if (isFinishing()) return;

                progressBar.setVisibility(View.GONE);

                safeLocations.clear();
                safeLocations.addAll(tempList);

                adapter.notifyDataSetChanged();
            });

        }).start();
    }
}