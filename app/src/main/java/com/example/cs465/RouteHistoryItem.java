package com.example.cs465;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class RouteHistoryItem {
    private final String origin;
    private final String destination;
    private final long timestamp;

    public List<LatLng> polylinePoints;
    public LatLng start;
    public LatLng end;
    public List<LatLng> intermediates;

    public RouteHistoryItem(String origin,
                            String destination,
                            long timestamp,
                            List<LatLng> polylinePoints,
                            LatLng start,
                            LatLng end,
                            List<LatLng> intermediates) {
        this.origin = origin;
        this.destination = destination;
        this.timestamp = timestamp;
        this.polylinePoints = polylinePoints;
        this.start = start;
        this.end = end;
        this.intermediates = intermediates;
    }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public long getTimestamp() { return timestamp; }

    public List<LatLng> getPolylinePoints() { return polylinePoints; }

    public LatLng getStart() { return start; }
    public LatLng getEnd() { return end; }

    public List<LatLng> getIntermediates() { return intermediates; }
}
