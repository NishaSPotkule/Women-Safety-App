package com.myandroid.nariguard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class SafeLocationMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_safe_location_map);

        MapView mapView = findViewById(R.id.map);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(17);

        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);
        String name = getIntent().getStringExtra("name");

        GeoPoint point = new GeoPoint(lat, lng);
        mapView.getController().setCenter(point);

        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(name);
        mapView.getOverlays().add(marker);
    }
}
