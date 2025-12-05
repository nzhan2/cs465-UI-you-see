package com.example.cs465;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
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
                list.add(new RouteHistoryItem(
                        obj.getString("origin"),
                        obj.getString("destination"),
                        obj.getLong("timestamp")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static void clearRoutes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROUTES, "[]").apply();
    }

}
