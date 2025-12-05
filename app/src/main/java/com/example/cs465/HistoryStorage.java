package com.example.cs465;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HistoryStorage {
    private static final String PREF_NAME = "route_history";
    private static final String KEY_ROUTES = "routes";

    public static void saveRoute(Context context, RouteHistoryItem item) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(prefs.getString(KEY_ROUTES, "[]"));
            JSONObject obj = new JSONObject();
            obj.put("origin", item.getOrigin());
            obj.put("destination", item.getDestination());
            obj.put("timestamp", item.getTimestamp());
            obj.put("polylinePoints", latLngListToJson(item.getPolylinePoints()));
            obj.put("start", latLngToJson(item.getStart()));
            obj.put("end", latLngToJson(item.getEnd()));
            obj.put("intermediates", latLngListToJson(item.getIntermediates()));
            arr.put(obj);
            prefs.edit().putString(KEY_ROUTES, arr.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<RouteHistoryItem> getRoutes(Context context) {
        List<RouteHistoryItem> list = new ArrayList<>();
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(prefs.getString(KEY_ROUTES, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                JSONArray polyArr = obj.getJSONArray("polylinePoints");
                JSONArray intermediatesArr = obj.getJSONArray("intermediates");
                RouteHistoryItem routeHistoryItem = new RouteHistoryItem(
                        obj.getString("origin"),
                        obj.getString("destination"),
                        obj.getLong("timestamp"),
                        parseLatLngArray(polyArr),
                        parseLatLng(obj.getJSONObject("start")),
                        parseLatLng(obj.getJSONObject("end")),
                        parseLatLngArray(intermediatesArr));
                list.add(routeHistoryItem);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static void clearRoutes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROUTES, "[]").apply();
    }
    private static List<LatLng> parseLatLngArray(JSONArray arr) throws JSONException {
        List<LatLng> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject point = arr.getJSONObject(i);
            list.add(new LatLng(
                    point.getDouble("latitude"),
                    point.getDouble("longitude")
            ));
        }
        return list;
    }

    private static LatLng parseLatLng(JSONObject obj) throws JSONException {
        return new LatLng(
                obj.getDouble("latitude"),
                obj.getDouble("longitude")
        );
    }

    private static JSONObject latLngToJson(LatLng latLng) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("latitude", latLng.latitude);
        obj.put("longitude", latLng.longitude);
        return obj;
    }

    private static JSONArray latLngListToJson(List<LatLng> list) throws JSONException {
        JSONArray arr = new JSONArray();
        for (LatLng p: list) {
            arr.put(latLngToJson(p));
        }
        return arr;
    }
}
