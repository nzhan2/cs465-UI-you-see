package com.example.cs465;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RouteInfo {
    public List<LatLng> path;
    public String distanceText;
    public String durationText;

    // New fields for step navigation
    public List<String> instructions = new ArrayList<>();

    public RouteInfo(List<LatLng> path, String distanceText, String durationText) {
        this.path = path;
        this.distanceText = distanceText;
        this.durationText = durationText;
    }

    // New optional constructor
    public RouteInfo(List<LatLng> path, String distanceText, String durationText,
                     List<String> instructions) {
        this(path, distanceText, durationText);
        if (instructions != null) this.instructions = instructions;
    }
}

