package com.example.cs465;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class RouteInfo {
    public List<LatLng> path;
    public String distanceText;
    public String durationText;

    public RouteInfo(List<LatLng> path, String distanceText, String durationText) {
        this.path = path;
        this.distanceText = distanceText;
        this.durationText = durationText;
    }

}
