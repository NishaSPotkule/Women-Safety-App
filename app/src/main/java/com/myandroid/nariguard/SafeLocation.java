package com.myandroid.nariguard;

public class SafeLocation {

    public final String name;
    public final double lat;
    public final double lon;
    public final double distance;
    public final String type; // police or hospital

    public SafeLocation(String name,
                        double lat,
                        double lon,
                        double distance,
                        String type) {

        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.distance = distance;
        this.type = type;
    }

    // helper method (clean code)
    public boolean isPolice() {
        return "police".equalsIgnoreCase(type);
    }
}