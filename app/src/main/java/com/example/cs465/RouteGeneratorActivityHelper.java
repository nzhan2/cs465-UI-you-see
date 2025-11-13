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
import okhttp3.Request;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class RouteGeneratorActivityHelper {

    public interface RoutesCallback {
        void onRoutesFetched(List<List<LatLng>> routes);
    }

    public interface GeocodeCallback {
        void onGeocoded(LatLng latLng);
    }

    public static void getRoutes(LatLng origin, LatLng destination,
                                 List<LatLng> intermediaries,
                                 String apiKey,
                                 RoutesCallback routesCallback) {

        if (intermediaries == null || intermediaries.isEmpty()) {
            getSingleRoute(origin, destination, null, apiKey, routesCallback);
            return;
        }

        List<List<LatLng>> allRoutes = new ArrayList<>();
        List<List<LatLng>> permutations = generatePermutations(intermediaries);

        final int total = permutations.size();
        final int[] completed = {0};

        for (List<LatLng> midPoints : permutations) {

            getSingleRoute(origin, destination, midPoints, apiKey, singleRoute -> {
                synchronized (allRoutes) {
                    if (!singleRoute.isEmpty()) {
                        allRoutes.add(singleRoute.get(0));
                    }

                    completed[0]++;

                    if (completed[0] == total) {
                        routesCallback.onRoutesFetched(allRoutes);
                    }
                }
            });
        }
    }


    private static List<List<LatLng>> generatePermutations(List<LatLng> points) {
        List<List<LatLng>> results = new ArrayList<>();
        permute(points, 0, results);
        return results;
    }

    private static void permute(List<LatLng> arr, int k, List<List<LatLng>> results) {
        for (int i = k; i < arr.size(); i++) {
            Collections.swap(arr, i, k);
            permute(arr, k + 1, results);
            Collections.swap(arr, k, i);
        }
        if (k == arr.size() - 1) {
            results.add(new ArrayList<>(arr));
        }
    }


    private static void getSingleRoute(
            LatLng origin,
            LatLng destination,
            List<LatLng> intermediaries,
            String apiKey,
            RoutesCallback routesCallback
    ) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://routes.googleapis.com/directions/v2:computeRoutes?key=" + apiKey;

        JSONObject bodyJson = new JSONObject();

        try {
            JSONObject originLatLng = new JSONObject();
            originLatLng.put("latitude", origin.latitude);
            originLatLng.put("longitude", origin.longitude);

            JSONObject originLocation = new JSONObject();
            originLocation.put("latLng", originLatLng);

            JSONObject originObject = new JSONObject();
            originObject.put("location", originLocation);

            JSONObject destLatLng = new JSONObject();
            destLatLng.put("latitude", destination.latitude);
            destLatLng.put("longitude", destination.longitude);

            JSONObject destLocation = new JSONObject();
            destLocation.put("latLng", destLatLng);

            JSONObject destObject = new JSONObject();
            destObject.put("location", destLocation);

            bodyJson.put("origin", originObject);
            bodyJson.put("destination", destObject);

            if (intermediaries != null && !intermediaries.isEmpty()) {
                JSONArray intArray = new JSONArray();

                for (LatLng interm : intermediaries) {

                    JSONObject interLatLng = new JSONObject();
                    interLatLng.put("latitude", interm.latitude);
                    interLatLng.put("longitude", interm.longitude);

                    JSONObject interLocation = new JSONObject();
                    interLocation.put("latLng", interLatLng);

                    JSONObject interObject = new JSONObject();
                    interObject.put("location", interLocation);

                    intArray.put(interObject);
                }

                bodyJson.put("intermediates", intArray);
            }

            bodyJson.put("travelMode", "WALK");
            bodyJson.put("computeAlternativeRoutes", true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(bodyJson.toString(),
                        okhttp3.MediaType.parse("application/json")))
                .addHeader("X-Goog-FieldMask",
                        "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline")
                .build();


        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String jsonData = response.body().string();

                List<List<LatLng>> allRoutes = new ArrayList<>();

                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray routes = jsonObject.getJSONArray("routes");

                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject route = routes.getJSONObject(i);
                        String polyline = route.getJSONObject("polyline")
                                .getString("encodedPolyline");

                        List<LatLng> decodePath = PolyUtil.decode(polyline);
                        allRoutes.add(decodePath);
                    }

                    new Handler(Looper.getMainLooper()).post(() ->
                            routesCallback.onRoutesFetched(allRoutes));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void geocodePlace(String placeName, String apiKey, GeocodeCallback callback) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + placeName.replace(" ", "+") + "&key=" + apiKey;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String jsonData = response.body().string();

                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray results = jsonObject.getJSONArray("results");

                    if (results.length() > 0) {
                        JSONObject loc = results.getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location");

                        double lat = loc.getDouble("lat");
                        double lng = loc.getDouble("lng");

                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onGeocoded(new LatLng(lat, lng)));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
