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
import android.content.Context;


public class RouteGeneratorActivityHelper {

    public interface RoutesCallback {
        void onRoutesFetched(List<RouteInfo> routes);
    }

    public interface GeocodeCallback {
        void onGeocoded(LatLng latLng);
    }

    public static void getRoutes(LatLng origin, LatLng destination, List<LatLng> intermediaries, String apiKey, String constraintType, double constraintValue, Context context, RoutesCallback routesCallback) {
        if (intermediaries == null || intermediaries.isEmpty()) {
            getSingleRoute(origin, destination, null, apiKey, constraintType, constraintValue, context, routesCallback);
            return;
        }

        List<RouteInfo> allRoutes = new ArrayList<>();
        List<List<LatLng>> permutations = (intermediaries == null || intermediaries.isEmpty()) ?
                Collections.singletonList(Collections.emptyList()) :
                generatePermutations(intermediaries);

        final int total = permutations.size();
        final int[] completed = {0};

        for (List<LatLng> midPoints: permutations) {
            getSingleRoute(origin, destination, midPoints, apiKey, constraintType, constraintValue, context, singleRoute -> {
                synchronized (allRoutes) {
                    if (!singleRoute.isEmpty()) {
                        allRoutes.addAll(singleRoute);
                    }
                    completed[0]++;
                    Log.d("RouteGeneratorHelper", "Received " + singleRoute.size() + " routes. Total so far: " + allRoutes.size());
                    if (completed[0] == total) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (allRoutes.isEmpty()) {
                                String message;
                                if ("distance".equals(constraintType)) {
                                    message = "No routes are within your +1 km tolerance.";
                                } else {
                                    message = "No routes are within your +10 min tolerance.";
                                }

                                new android.app.AlertDialog.Builder(context)
                                        .setTitle("No Routes Found Within Time/Distance Constraints")
                                        .setMessage(message)
                                        .setPositiveButton("OK", (dialog, which) -> {
                                            dialog.dismiss();
                                            if (context instanceof android.app.Activity) {
                                                ((android.app.Activity) context).finish();
                                            }
                                        })
                                        .show();
                            } else {
                                routesCallback.onRoutesFetched(allRoutes);
                            }
                        });
//                        Log.d("RouteGeneratorHelper", "All routes fetched! Total: " + allRoutes.size());
//                        routesCallback.onRoutesFetched(allRoutes);
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

    private static void getSingleRoute(LatLng origin, LatLng destination, List<LatLng> intermediaries, String apiKey, String constraintType, double constraintValue, Context context, RoutesCallback routesCallback) {
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
                String jsonData = response.body().string();
                Log.d("RouteHelper", "Routes API response: " + jsonData);
                List<RouteInfo> routeInfos = new ArrayList<>();
                RouteInfo closestRoute = null;
                double minDifference = Double.MAX_VALUE;

                try {
//                    JSONObject json = new JSONObject(response.body().string());
//                    JSONArray routes = json.getJSONArray("routes");
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray routes = jsonObject.getJSONArray("routes");
                  
                    for (int i = 0; i < routes.length(); i++) {
                        JSONObject route = routes.getJSONObject(i);
                        String polyline = route.getJSONObject("polyline").getString("encodedPolyline");

                        double distanceMeters = route.optDouble("distanceMeters", 0);
                        double durationSeconds = Double.parseDouble(route.optString("duration").replace("s", ""));

                        double distanceKm = distanceMeters / 1000.0;
                        double durationMinutes = durationSeconds / 60.0;

                        boolean passes = true;
                        double diff = 0;

                        if ("distance".equals(constraintType)) {
                            passes = distanceKm <= (constraintValue + 1.0);
                            diff = Math.abs(distanceKm - constraintValue);
                        } else if ("time".equals(constraintType)) {
                            passes = durationMinutes <= (constraintValue + 10.0);
                            diff = Math.abs(durationMinutes - constraintValue);
                        }

                        if (diff < minDifference) {
                            minDifference = diff;
                            closestRoute = new RouteInfo(PolyUtil.decode(polyline),
                                    String.format("%.1f km", distanceKm),
                                    (int)Math.ceil(durationMinutes) + " min");
                        }
                        if (passes) {
                            List<LatLng> decodePath = PolyUtil.decode(polyline);
                            String distanceText = String.format("%.1f km", distanceKm);
                            String durationText = (int)Math.ceil(durationMinutes) + " min";
                            routeInfos.add(new RouteInfo(decodePath, distanceText, durationText));
                        }
//                        String distanceText = String.format("%.1f km", distanceKm);
//                        String durationText = durationMinutes + "min";

//                        List<LatLng> decodePath = PolyUtil.decode(polyline);
//                        routeInfos.add(new RouteInfo(decodePath, distanceText, durationText));
                    }
                    final List<RouteInfo> finalRoutes = routeInfos;
                    new Handler(Looper.getMainLooper()).post(() -> routesCallback.onRoutesFetched(finalRoutes));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
//                    new Handler(Looper.getMainLooper()).post(() -> routesCallback.onRoutesFetched(routeInfos));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }



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
