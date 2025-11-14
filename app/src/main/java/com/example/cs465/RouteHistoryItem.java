package com.example.cs465;

public class RouteHistoryItem {
    private final String origin;
    private final String destination;
    private final long timestamp;

    public RouteHistoryItem(String origin, String destination, long timestamp) {
        this.origin = origin;
        this.destination = destination;
        this.timestamp = timestamp;
    }

    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public long getTimestamp() { return timestamp; }
}
