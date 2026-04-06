package com.myandroid.nariguard;

import android.Manifest;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private EditText etSearch;
    private IMapController mapController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // ✅ OSMDroid configuration
        Configuration.getInstance()
                .setUserAgentValue(requireActivity().getPackageName());

        // ✅ Map setup
        mapView = view.findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        mapController = mapView.getController();
        mapController.setZoom(16.0);

        // ✅ Location overlay (blue dot)
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(requireContext()),
                mapView);

        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Move camera when location found
        myLocationOverlay.runOnFirstFix(() ->
                requireActivity().runOnUiThread(() -> {
                    GeoPoint myPoint = myLocationOverlay.getMyLocation();
                    if (myPoint != null) {
                        mapController.animateTo(myPoint);
                        mapController.setZoom(17.0);
                    }
                }));

        // ✅ Search UI
        etSearch = view.findViewById(R.id.etSearch);
        ImageButton btnSearch = view.findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> searchLocation());

        // ✅ LIVE SEARCH while typing
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
            @Override public void afterTextChanged(Editable s){}

            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                if (s.length() > 3) {
                    searchLocationLive(s.toString());
                }
            }
        });


        ImageButton btnMyLocation = view.findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(v -> {
            GeoPoint myPoint = myLocationOverlay.getMyLocation();
            if (myPoint != null)
                mapController.animateTo(myPoint);
            else
                Toast.makeText(getActivity(),"Getting location...",Toast.LENGTH_SHORT).show();
        });


        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },1);

        return view;
    }



    private void searchLocation() {

        String locationName = etSearch.getText().toString().trim();

        if (locationName.isEmpty()) {
            Toast.makeText(getActivity(),"Enter location",Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {

                GeocoderNominatim geocoder =
                        new GeocoderNominatim("NariGuard");

                List<android.location.Address> results =
                        geocoder.getFromLocationName(locationName,1);

                if(results != null && !results.isEmpty()) {

                    android.location.Address address = results.get(0);

                    GeoPoint destination =
                            new GeoPoint(address.getLatitude(),
                                    address.getLongitude());

                    requireActivity().runOnUiThread(() -> {


                        mapView.getOverlays().removeIf(o ->
                                o instanceof Marker || o instanceof Polyline);

                        Marker marker = new Marker(mapView);
                        marker.setPosition(destination);
                        marker.setTitle(locationName);
                        mapView.getOverlays().add(marker);

                        mapController.animateTo(destination);


                        GeoPoint start = myLocationOverlay.getMyLocation();
                        if(start != null)
                            drawRoute(start, destination);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }



    private void searchLocationLive(String query) {

        new Thread(() -> {
            try {
                GeocoderNominatim geocoder =
                        new GeocoderNominatim("NariGuard");

                List<android.location.Address> results =
                        geocoder.getFromLocationName(query,1);

                if(results != null && !results.isEmpty()) {

                    android.location.Address address = results.get(0);
                    GeoPoint point =
                            new GeoPoint(address.getLatitude(),
                                    address.getLongitude());

                    requireActivity().runOnUiThread(() ->
                            mapController.animateTo(point));
                }

            } catch (Exception ignored) {}
        }).start();
    }



    private void drawRoute(GeoPoint start, GeoPoint end) {

        new Thread(() -> {

            OSRMRoadManager roadManager =
                    new OSRMRoadManager(requireContext(),"NariGuard");

            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(start);
            waypoints.add(end);

            Road road = roadManager.getRoad(waypoints);

            requireActivity().runOnUiThread(() -> {

                Polyline roadOverlay =
                        RoadManager.buildRoadOverlay(road);

                mapView.getOverlays().add(roadOverlay);
                mapView.invalidate();
            });

        }).start();
    }



    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}