package com.example.cs465;

import static android.graphics.Color.argb;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.pm.ApplicationInfo;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;



public class RouteGeneratorActivity extends FragmentActivity  implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
    private GoogleMap mMap;

    private Button saveButton;
    private Button navButton;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    String apiKey;

    private List<LatLng> savedIntermediaryLatLngs = new ArrayList<>();

    private List<LatLng> selectedRoutePoints = null;
    private LatLng selectedStart = null;
    private LatLng selectedEnd = null;

    interface OnAllGeocodedListener {
        void onAllGeocoded(List<LatLng> allLatLngs);
    }
    int[] routeColors = {
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.MAGENTA,
            Color.CYAN
    };

    private TextView routeInfoTextView;

    private String start;
    private String end;
    private ArrayList<String> intermediateLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        routeInfoTextView = findViewById(R.id.routeInfoTextView);

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        start = getIntent().getStringExtra("start");
        end = getIntent().getStringExtra("end");

        /*start="Illini Union";
        end="Foellinger Auditorium";*/

        intermediateLocations = getIntent().getStringArrayListExtra("intermediates");
        //intermediateLocations.add("ARC Champaign");

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish(); // returns to MainActivity (the previous screen)
        });

        saveButton = findViewById(R.id.saveButton);
        navButton = findViewById(R.id.navButton);
        saveButton.setVisibility(View.GONE);
        navButton.setVisibility(View.GONE);

        saveButton.setOnClickListener(v -> {
            if (selectedRoutePoints == null || selectedStart == null || selectedEnd == null) {
                Toast.makeText(this, "Route not fully defined", Toast.LENGTH_SHORT).show();
                return;
            }

//            String routeName = "Route " + System.currentTimeMillis(); // replace with user input if needed

            SharedPreferences prefs = getSharedPreferences("routes", MODE_PRIVATE);
            Gson gson = new Gson();

            // Load existing saved routes
            String existingJson = prefs.getString("saved_routes_list", null);
            ArrayList<SavedRoute> savedRoutes;
            if (existingJson != null) {
                Type listType = new TypeToken<ArrayList<SavedRoute>>(){}.getType();
                savedRoutes = gson.fromJson(existingJson, listType);
            } else {
                savedRoutes = new ArrayList<>();
            }
            // Determine next route number
            int routeNumber = savedRoutes.size() + 1;
            String routeName = "Route " + routeNumber;

// Save current timestamp instead of route info
            long timestamp = System.currentTimeMillis();
            SavedRoute newRoute = new SavedRoute(
                    routeName,
                    new ArrayList<>(selectedRoutePoints),
                    selectedStart,
                    selectedEnd,
                    savedIntermediaryLatLngs
            );
            // Add the new route
            savedRoutes.add(newRoute);

            // Save back to SharedPreferences
            prefs.edit().putString("saved_routes_list", gson.toJson(savedRoutes)).apply();

            Toast.makeText(this, "Route saved!", Toast.LENGTH_SHORT).show();
        });

//        saveButton.setOnClickListener(v -> {
//            LatLng currentLatLng = null;
//            Location myLocation = mMap.getMyLocation();
//            if (myLocation != null) {
//                currentLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//            } else {
//                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (selectedRoutePoints == null) {
//                Toast.makeText(this, "No route selected", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Build saved route
//            SavedRoute route = new SavedRoute(
//                    "Route from " + start + " to " + end,
//                    selectedRoutePoints,
//                    selectedStart,
//                    selectedEnd,
//                    savedIntermediaryLatLngs
//            );
//
//            RouteStorage.saveRoute(RouteGeneratorActivity.this, route);
//            Toast.makeText(this, "Route saved!", Toast.LENGTH_SHORT).show();
//
//            SharedPreferences prefs = getSharedPreferences("routes", MODE_PRIVATE);
//            SharedPreferences.Editor edit = prefs.edit();
//
//            String routeJson = new Gson().toJson(selectedRoutePoints);
//            long timestamp = System.currentTimeMillis();
//
//            edit.putString("route_points", routeJson);
//            edit.putString("userLocation", new Gson().toJson(currentLatLng));
//            edit.putLong("route_timestamp", timestamp);
//            edit.apply();
//
//        });
//
//
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setMyLocationButtonEnabled(false);

        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(20.0f);

        String originName = start;
        String destinationName = end;

        List<String> intermediaries = intermediateLocations;
        Log.d("debug", "intermediaries size: " + intermediaries.size());

        RouteGeneratorActivityHelper.geocodePlace(originName, apiKey, originLatLng -> {
            RouteGeneratorActivityHelper.geocodePlace(destinationName, apiKey, destLatLng -> {
                geocodeAllPlaces(intermediaries, apiKey, intermediaryLatLngs -> {
                    RouteGeneratorActivityHelper.getRoutes(originLatLng, destLatLng, intermediaryLatLngs, apiKey, routes -> {
//                        StringBuilder infoBuilder = new StringBuilder();
                        SpannableStringBuilder infoBuilder = new SpannableStringBuilder();
                        savedIntermediaryLatLngs = intermediaryLatLngs;

                        List<Polyline> polylines = new ArrayList<>();

                        for (int i = 0; i < routes.size(); i++) {
                            RouteInfo route = routes.get(i);
                            int color = routeColors[i % routeColors.length];

                            // Draw the route
                            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                                    .addAll(route.path)
                                    .color(color)
                                    .width(10f));

                            polylines.add(polyline);

                            // Build text line
                            String text = "Route " + (i + 1) + ": " + route.distanceText + ", " + route.durationText + "\n";
                            int start = infoBuilder.length();
                            infoBuilder.append(text);
                            int end = infoBuilder.length();

                            // Make line clickable
                            final int index = i; // for lambda
                            infoBuilder.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View widget) {
                                    selectRoute(index, polylines);
                                }
                            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            infoBuilder.setSpan(
                                    new ForegroundColorSpan(color),
                                    start, end,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            );
                        }

                        routeInfoTextView.setText(infoBuilder.toString());
                        routeInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());
                        routeInfoTextView.setText(infoBuilder);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng point: routes.get(0).path) {
                            builder.include(point);
                        }

                        LatLngBounds bounds = builder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        mMap.setOnPolylineClickListener(polyline ->
                                Log.d("Map", "Clicked " + polyline.getTag())
                        );
                    });
                });
            });
        });
        routeInfoTextView.post(() -> {
            int paddingBottom = routeInfoTextView.getHeight() + 400;
            mMap.setPadding(0, 0, 0, paddingBottom);
        });

    }

    private void selectRoute(int selectedIndex, List<Polyline> polylines) {
        for (int i = 0; i < polylines.size(); i++) {
            Polyline p = polylines.get(i);

            if (i == selectedIndex) {
                // Highlight selected route
                p.setWidth(20f);       // thicker line
                p.setZIndex(10);       // bring forward
            } else {
                // Normal line — NOT greyed out
                p.setWidth(10f);
                p.setZIndex(1);
            }
        }

        // save route
        selectedRoutePoints = new ArrayList<>(polylines.get(selectedIndex).getPoints());
        selectedStart = selectedRoutePoints.get(0);
        selectedEnd = selectedRoutePoints.get(selectedRoutePoints.size() - 1);


        List<LatLng> points = polylines.get(selectedIndex).getPoints();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) builder.include(point);

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));

        saveButton.setVisibility(View.VISIBLE);
        navButton.setVisibility(View.VISIBLE);

        navButton.setOnClickListener(v -> {
            Location myLocation = mMap.getMyLocation();
            if (myLocation == null) {
                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
                return;
            }

            LatLng currentLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            LatLng routeStart = points.get(0);
            LatLng routeEnd = points.get(points.size() - 1);

            ArrayList<String> intermediaryLatLings = getIntent().getStringArrayListExtra("intermediates");


//            List<LatLng> intermediaryLatLngs = new ArrayList<>();
//
//            if (intermediaryLatLings != null) {
//                for (String s : intermediaryLatLings) {
////                    String[] parts = s.split(",");
////                    double lat = Double.parseDouble(parts[0]);
////                    double lng = Double.parseDouble(parts[1]);
////                    intermediaryLatLngs.add(new LatLng(lat, lng));
//                    if (!s.contains(",")) {
//                        Log.e("ROUTE", "Skipping non-coordinate string: " + s);
//                        continue;
//                    }
//
//                    try {
//                        String[] parts = s.split(",");
//                        double lat = Double.parseDouble(parts[0]);
//                        double lng = Double.parseDouble(parts[1]);
//                        intermediaryLatLngs.add(new LatLng(lat, lng));
//                    } catch (NumberFormatException e) {
//                        Log.e("ROUTE", "Invalid coordinate format: " + s);
//                    }
//                }
//            }

//            LatLng landmark1 = intermediaryLatLngs.size() > 0 ? intermediaryLatLngs.get(0) : null;
//            LatLng landmark2 = intermediaryLatLngs.size() > 1 ? intermediaryLatLngs.get(1) : null;
            LatLng landmark1 = savedIntermediaryLatLngs.size() > 0 ? savedIntermediaryLatLngs.get(0) : null;
            LatLng landmark2 = savedIntermediaryLatLngs.size() > 1 ? savedIntermediaryLatLngs.get(1) : null;


            Intent intent = new Intent(RouteGeneratorActivity.this, NavigationActivity.class);
            intent.putParcelableArrayListExtra("routePoints", new ArrayList<>(points));
            intent.putExtra("userLocation", currentLatLng);
            intent.putExtra("startPoint", routeStart);
            intent.putExtra("endPoint", routeEnd);
            intent.putExtra("landmark1", landmark1);
            intent.putExtra("landmark2", landmark2);
            startActivity(intent);



//            Intent intent = new Intent(RouteGeneratorActivity.this, NavigationActivity.class);
//            intent.putParcelableArrayListExtra("routePoints", (ArrayList<? extends Parcelable>) points); // selected route
//            intent.putExtra("userLocation", currentLatLng); // current location
//            startActivity(intent);

//            Location myLocation = mMap.getMyLocation();
//            if (myLocation == null) {
//                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            LatLng currentLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//            LatLng routeStart = points.get(0);
//
//            // Draw connector line (current → start)
//            Polyline connector = mMap.addPolyline(new PolylineOptions()
//                    .add(currentLatLng, routeStart)
//                    .color(Color.RED)  // different color from route
//                    .width(12f));
//
//            // Zoom camera to fit both route + connector
//            LatLngBounds.Builder navBounds = new LatLngBounds.Builder();
//            navBounds.include(currentLatLng);
//            for (LatLng point : points) navBounds.include(point);
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(navBounds.build(), 150));
        });
    }

    private void clearSelection(List<Polyline> polylines) {
        for (Polyline p : polylines) {
            p.setWidth(10f);
            p.setZIndex(1);
        }

        saveButton.setVisibility(View.GONE);
        navButton.setVisibility(View.GONE);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

//    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

//    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Permission granted → enable location
                enableMyLocation();
            } else {
                // Permission denied → show message or disable location features
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void geocodeAllPlaces(List<String> placeNames, String apiKey, OnAllGeocodedListener listener) {
        List<LatLng> results = new ArrayList<>();

        if (placeNames.isEmpty()) {
            listener.onAllGeocoded(results);
            return;
        }

        geocodeNextPlace(0, placeNames, results, apiKey, listener);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Location permission is needed to show your current position on the map.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void geocodeNextPlace(int index, List<String> placeNames, List<LatLng> results, String apiKey, OnAllGeocodedListener listener) {
        if (index >= placeNames.size()) {
            listener.onAllGeocoded(results);
            return;
        }

        String name = placeNames.get(index);
        Log.d("Test", "name: " + name);
        RouteGeneratorActivityHelper.geocodePlace(name, apiKey, latLng -> {
            results.add(latLng);
            geocodeNextPlace(index + 1, placeNames, results, apiKey, listener);
        });
    }
}

