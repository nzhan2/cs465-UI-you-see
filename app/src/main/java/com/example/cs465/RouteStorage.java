package com.example.cs465;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class RouteStorage {
    private static final String PREFS = "saved_routes";
    private static final String KEY = "routes_json";

    public static void saveRoute(Context ctx, SavedRoute route) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();

        // Get existing routes
        String json = prefs.getString(KEY, "[]");
        Type listType = new TypeToken<List<SavedRoute>>(){}.getType();
        List<SavedRoute> routes = gson.fromJson(json, listType);

        // Add new one
        routes.add(route);

        // Save updated list
        prefs.edit().putString(KEY, gson.toJson(routes)).apply();
    }

    public static List<SavedRoute> loadRoutes(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY, "[]");
        Type listType = new TypeToken<List<SavedRoute>>(){}.getType();
        return new Gson().fromJson(json, listType);
    }
}
