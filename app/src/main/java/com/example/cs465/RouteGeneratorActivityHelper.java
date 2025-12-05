package com.example.cs465;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RouteGeneratorActivityHelper {

    public interface RoutesCallback {
        void onRoutesFetched(List<RouteInfo> routes);
    }

    public interface GeocodeCallback {
        void onGeocoded(LatLng latLng);
    }

    public static void getRoutes(LatLng origin, LatLng destination,
                                 List<LatLng> intermediaries,
                                 String apiKey,
                                 RoutesCallback callback) {

        if (intermediaries == null || intermediaries.isEmpty()) {
            // No stops -> just return one route
            getSingleRoute(origin, destination, null, apiKey, callback);
            return;
        }

        List<List<LatLng>> permutations = generatePermutations(intermediaries);

        List<RouteInfo> allRoutes = new ArrayList<>();
        final int total = permutations.size();
        final int[] done = {0};

        for (List<LatLng> order : permutations) {
            getSingleRoute(origin, destination, order, apiKey, partialList -> {
                synchronized (allRoutes) {
                    // Convert List<List<LatLng>> to RouteInfo list
                    for (RouteInfo r : partialList) {
                        allRoutes.add(r);
                    }
                    done[0]++;

                    if (done[0] == total) {
                        callback.onRoutesFetched(allRoutes);
                    }
                }
            });
        }
    }

    private static List<List<LatLng>> generatePermutations(List<LatLng> list) {
        List<List<LatLng>> results = new ArrayList<>();
        permute(list, 0, results);
        return results;
    }

    private static void permute(List<LatLng> arr, int start, List<List<LatLng>> out) {
        for (int i = start; i < arr.size(); i++) {
            Collections.swap(arr, i, start);
            permute(arr, start + 1, out);
            Collections.swap(arr, start, i);
        }
        if (start == arr.size() - 1) {
            out.add(new ArrayList<>(arr));
        }
    }

    private static void getSingleRoute(LatLng origin,
                                       LatLng destination,
                                       List<LatLng> intermediaries,
                                       String apiKey,
                                       RoutesCallback callback) {

        OkHttpClient client = new OkHttpClient();
        String url = "https://routes.googleapis.com/directions/v2:computeRoutes?key=" + apiKey;

        JSONObject body = new JSONObject();
        try {
            body.put("origin", locationObject(origin));
            body.put("destination", locationObject(destination));

            if (intermediaries != null && !intermediaries.isEmpty()) {
                JSONArray arr = new JSONArray();
                for (LatLng ll : intermediaries) arr.put(locationObject(ll));
                body.put("intermediates", arr);
            }

            body.put("travelMode", "WALK");
            body.put("computeAlternativeRoutes", true);
        } catch (Exception ignored) {}

        Request request = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(
                        body.toString(),
                        okhttp3.MediaType.parse("application/json")))
                .addHeader("X-Goog-FieldMask",
                        "routes.distanceMeters," +
                                "routes.duration," +
                                "routes.polyline.encodedPolyline," +
                                "routes.legs.steps.polyline.encodedPolyline," +
                                "routes.legs.steps.navigationInstruction")

                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                List<RouteInfo> parsed = new ArrayList<>();

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray arr = json.getJSONArray("routes");
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject r = arr.getJSONObject(i);

                        double meters = r.getDouble("distanceMeters");
                        double secs = Double.parseDouble(r.getString("duration")
                                .replace("s", ""));

                        String dist = String.format("%.1f km", meters / 1000.0);
                        String dur = ((int) Math.ceil(secs / 60.0)) + " min";

                        List<LatLng> path = PolyUtil.decode(
                                r.getJSONObject("polyline")
                                        .getString("encodedPolyline")
                        );

                        List<String> steps = extractInstructions(r);
                        parsed.add(new RouteInfo(path, dist, dur, steps));

                    }
                } catch (Exception ignored) {}

                new Handler(Looper.getMainLooper())
                        .post(() -> callback.onRoutesFetched(parsed));
            }
        });
    }



    private static JSONObject locationObject(LatLng ll) throws Exception {
        JSONObject pos = new JSONObject();
        pos.put("latitude", ll.latitude);
        pos.put("longitude", ll.longitude);
        JSONObject loc = new JSONObject();
        loc.put("latLng", pos);
        JSONObject ret = new JSONObject();
        ret.put("location", loc);
        return ret;
    }

    public static void geocodePlace(String place, String key, GeocodeCallback cb) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                place.replace(" ", "+") + "&key=" + key;

        client.newCall(new Request.Builder().url(url).build())
                .enqueue(new Callback() {
                    @Override public void onFailure(Call c, IOException e) {}

                    @Override
                    public void onResponse(Call c, Response r) throws IOException {
                        try {
                            JSONObject json = new JSONObject(r.body().string());
                            JSONObject loc = json.getJSONArray("results")
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location");
                            LatLng res = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));
                            new Handler(Looper.getMainLooper()).post(() -> cb.onGeocoded(res));
                        } catch (Exception ignored) {}
                    }
                });
    }


    private static List<String> extractInstructions(JSONObject routeJson) {
        List<String> list = new ArrayList<>();
        try {
            JSONArray legs = routeJson.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            JSONArray steps = leg.getJSONArray("steps");

            for (int i = 0; i < steps.length(); i++) {
                JSONObject step = steps.getJSONObject(i);
                JSONObject navInst = step.optJSONObject("navigationInstruction");
                if (navInst != null) {
                    list.add(navInst.getString("instructions"));
                }
            }
        } catch (Exception ignored) {}
        return list;
    }

}
