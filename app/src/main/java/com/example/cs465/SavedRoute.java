package com.example.cs465;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class SavedRoute {
    public String name;  // Optional name user gives
    public List<LatLng> polylinePoints;
    public LatLng start;
    public LatLng end;
    public List<LatLng> intermediates;

    public SavedRoute(String name, List<LatLng> polylinePoints, LatLng start, LatLng end, List<LatLng> intermediates) {
        this.name = name;
        this.polylinePoints = polylinePoints;
        this.start = start;
        this.end = end;
        this.intermediates = intermediates;
    }
}


