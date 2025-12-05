package com.example.cs465;

import static android.graphics.Color.argb;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;


public class RouteGeneratorActivity extends FragmentActivity  implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
    private GoogleMap mMap;

    private Button saveButton;
    private Button navButton;

    private Button popupSaveButton;
    private Button popupNavButton;

    private Button popupExportButton;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    String apiKey;

    private List<LatLng> savedIntermediaryLatLngs = new ArrayList<>();

    private List<LatLng> selectedRoutePoints = null;
    private LatLng selectedStart = null;
    private LatLng selectedEnd = null;

    private Map<String, Integer> landmarkImageMap = new HashMap<String, Integer>() {{
        put("ARC 1", R.drawable.arc_1);
        put("ARC 2", R.drawable.arc_2);
        put("CRCE 1", R.drawable.crce_1);
        put("CRCE 2", R.drawable.crce_2);
        put("default", R.drawable.alma);
    }};


    interface OnAllGeocodedListener {
        void onAllGeocoded(List<LatLng> allLatLngs);
    }
    int[] routeColors = {
            Color.parseColor("#FDCA40"),
            Color.parseColor("#33B5E5"),
            Color.parseColor("#5FAD56"),
            Color.parseColor("#FF6978"),
            Color.parseColor("#6D435A"),
    };

    private ScrollView routeInfoTextView;

    private String start;
    private String end;
    private String measure;
    private String value;
    private ArrayList<String> intermediateLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        routeInfoTextView = findViewById(R.id.routeInfoScrollView);

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
        measure = getIntent().getStringExtra("measure");
        value = getIntent().getStringExtra("value");

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

        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                Log.e("MapStyle", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapStyle", "Can't find style. Error: ", e);
        }

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

        EditText constraintValueInput = findViewById(R.id.constraintValueInput);

        String constraintType = "distance"; // default
        if (measure.equals("distance")) {
            constraintType = "distance";
        } else if (measure.equals("time")) {
            constraintType = "time";
        }

        double constraintValue = 0;
        try {
            constraintValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        final String finalConstraintType = constraintType;
        final double finalConstraintValue = constraintValue;

        RouteGeneratorActivityHelper.geocodePlace(originName, apiKey, originLatLng -> {
            RouteGeneratorActivityHelper.geocodePlace(destinationName, apiKey, destLatLng -> {
                geocodeAllPlaces(intermediaries, apiKey, intermediaryLatLngs -> {
                    RouteGeneratorActivityHelper.getRoutes(originLatLng, destLatLng, intermediaryLatLngs, apiKey, finalConstraintType, finalConstraintValue, RouteGeneratorActivity.this, routes -> {
//                        StringBuilder infoBuilder = new StringBuilder();
                        SpannableStringBuilder infoBuilder = new SpannableStringBuilder();
                        savedIntermediaryLatLngs = intermediaryLatLngs;
                        Log.d("DEBUG_STEPS", routes.get(0).instructions.toString());


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
                                    selectRoute(index, polylines, routes);

                                }
                            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                            infoBuilder.setSpan(
                                    new ForegroundColorSpan(color),
                                    start, end,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            );
                        }

                        mMap.addMarker(new MarkerOptions()
                                .position(originLatLng)
                                .title("Start: " + start)
                                .icon(getPinMarker("#6abd62", "S")) //green
                        );

                        mMap.addMarker(new MarkerOptions()
                                .position(destLatLng)
                                .title("End: " + end)
                                .icon(getPinMarker("#cf4e3a", "E")) //red
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        );

//                        for (int i = 0; i < intermediaryLatLngs.size(); i++) {
//                            LatLng stopLatLng = intermediaryLatLngs.get(i);
//                            String stopName = intermediaries.get(i);
//
//                            mMap.addMarker(new MarkerOptions()
//                                            .position(stopLatLng)
//                                            .title(stopName)          // The name the user entered
//                                            .snippet("Stop " + (i + 1))  // optional: a snippet or description
//                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))               // optional: set a custom marker icon here
//                            );
//                        }

                        for (int i = 0; i < intermediaryLatLngs.size(); i++) {
                            LatLng stopLatLng = intermediaryLatLngs.get(i);
                            String stopName = intermediaries.get(i);

                            mMap.addMarker(new MarkerOptions()
                                    .position(stopLatLng)
                                    .title("Stop " + (i + 1) + ": " + stopName)
                                    .icon(createNumberedMarker(this, i + 1, "#0f69fa"))
                            );
                        }


//                        routeInfoTextView.setText(infoBuilder.toString());
//                        routeInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());
//                        routeInfoTextView.setText(infoBuilder);

                        LinearLayout container = findViewById(R.id.routeInfoContainer);
                        container.removeAllViews();

                        for (int i = 0; i < routes.size(); i++) {

                            RouteInfo route = routes.get(i);
                            int color = routeColors[i % routeColors.length];

//

                            TextView routeItem = new TextView(this);

// --- TEXT ---
                            routeItem.setText("Route " + (i + 1));
                            routeItem.setTextSize(18);
                            routeItem.setTextColor(Color.WHITE);
                            routeItem.setGravity(Gravity.CENTER);

// --- APPEARANCE ---
                            routeItem.setPadding(40, 40, 40, 40);
                            GradientDrawable bg = new GradientDrawable();
                            bg.setColor(color);
                            bg.setCornerRadius(40f);
                            routeItem.setBackground(bg);

                            routeItem.setElevation(6f);

// Layout params (full width, margin)
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 20, 0, 20);
                            routeItem.setLayoutParams(params);

// Click behavior
                            final int index = i;
                            routeItem.setOnClickListener(v -> {
                                // Highlight route on map + store selectedPoint list
                                selectRoute(index, polylines, routes);


                                // Get the selected route data (path + instructions)
                                RouteInfo selectedRoute = routes.get(index);

                                showRouteDetailsPopup(
                                        "Route " + (index + 1),
                                        selectedRoute.distanceText,
                                        selectedRoute.durationText,

                                        // SAVE PRESSED
                                        () -> {
                                            if (selectedRoutePoints == null ||
                                                    selectedStart == null ||
                                                    selectedEnd == null) {
                                                Toast.makeText(this,
                                                        "Route not fully defined",
                                                        Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            SharedPreferences prefs = getSharedPreferences("routes", MODE_PRIVATE);
                                            Gson gson = new Gson();

                                            String existingJson = prefs.getString("saved_routes_list", null);
                                            ArrayList<SavedRoute> savedRoutes;

                                            if (existingJson != null) {
                                                Type listType = new TypeToken<ArrayList<SavedRoute>>(){}.getType();
                                                savedRoutes = gson.fromJson(existingJson, listType);
                                            } else {
                                                savedRoutes = new ArrayList<>();
                                            }

                                            int routeNumber = savedRoutes.size() + 1;
                                            String routeName = "Route " + routeNumber;

                                            SavedRoute newRoute = new SavedRoute(
                                                    routeName,
                                                    new ArrayList<>(selectedRoutePoints),
                                                    selectedStart,
                                                    selectedEnd,
                                                    savedIntermediaryLatLngs
                                            );

                                            savedRoutes.add(newRoute);

                                            prefs.edit()
                                                    .putString("saved_routes_list", gson.toJson(savedRoutes))
                                                    .apply();

                                            Toast.makeText(this,
                                                    "Route saved!",
                                                    Toast.LENGTH_SHORT).show();
                                        },

                                        // NAVIGATE PRESSED popupnavbutton
                                        () -> {
                                            Intent intent = new Intent(
                                                    RouteGeneratorActivity.this,
                                                    NavigationActivity.class
                                            );

                                            intent.putParcelableArrayListExtra(
                                                    "routePoints",
                                                    new ArrayList<>(selectedRoutePoints)
                                            );

                                            // Markers
                                            intent.putExtra("startPoint", selectedStart);
                                            intent.putExtra("endPoint", selectedEnd);

                                            // Route polyline color
                                            intent.putExtra("routeColor", routeColors[index % routeColors.length]);

                                            // Pass stop markers (intermediary latlngs)
                                            intent.putParcelableArrayListExtra(
                                                    "stopPoints",
                                                    new ArrayList<>(savedIntermediaryLatLngs)
                                            );

                                            // Pass their names too
                                            intent.putStringArrayListExtra(
                                                    "stopNames",
                                                    new ArrayList<>(intermediateLocations)
                                            );

                                            startActivity(intent);
                                        }


                                );
                            });



                            container.addView(routeItem);
                        }


                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        if (!routes.isEmpty()) {
                            for (LatLng point: routes.get(0).path) {
                                builder.include(point);
                            }

                            LatLngBounds bounds = builder.build();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                            mMap.setOnPolylineClickListener(polyline ->
                                    Log.d("Map", "Clicked " + polyline.getTag())
                            );
                        }
                    });
                });
            });
        });
        routeInfoTextView.post(() -> {
            int paddingBottom = routeInfoTextView.getHeight() + 400;
            mMap.setPadding(0, 0, 0, paddingBottom);
        });

    }

    private BitmapDescriptor getPinMarker(String hexColor, String text) {
        int color = Color.parseColor(hexColor);

        int width = 120;
        int height = 160;
        int size = 100;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50f);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        // Draw circle head
//        canvas.drawCircle(width / 2f, width / 2f, width / 2.5f, paint);
        float circleCenter = width / 2f;
        float circleRadius = width / 2.8f;   // slightly smaller, looks better
        canvas.drawCircle(circleCenter, circleCenter, circleRadius, paint);

        // Draw triangle body
        Path triangle = new Path();
        float triangleTopY = circleCenter;   // raised up
        float triangleBottomY = height * 0.92f;                   // bottom tip

        triangle.moveTo(circleCenter, triangleBottomY);                // tip
        triangle.lineTo(circleCenter - circleRadius * 0.9f, triangleTopY);  // left
        triangle.lineTo(circleCenter + circleRadius * 0.9f, triangleTopY);  // right
        triangle.close();

        canvas.drawPath(triangle, paint);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = width / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(text, width / 2f, textY, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor createNumberedMarker(Context context, int number, String hexColor) {
        int size = 100;  // marker image size

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor(hexColor));
        circlePaint.setAntiAlias(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50f);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        // Draw circle (marker background)
        canvas.drawCircle(size / 2f, size / 2f, size / 2.3f, circlePaint);

        // Draw number in center
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = size / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(String.valueOf(number), size / 2f, textY, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private void selectRoute(int selectedIndex, List<Polyline> polylines, List<RouteInfo> routes) {

        // Highlight selected polyline
        for (int i = 0; i < polylines.size(); i++) {
            Polyline p = polylines.get(i);
            if (i == selectedIndex) {
                p.setWidth(20f);
                p.setZIndex(10);
            } else {
                p.setWidth(10f);
                p.setZIndex(1);
            }
        }

        // Get correct matching RouteInfo data
        RouteInfo selected = routes.get(selectedIndex);

        // Assign the TRUE route path
        selectedRoutePoints = new ArrayList<>(selected.path);
        selectedStart = selectedRoutePoints.get(0);
        selectedEnd = selectedRoutePoints.get(selectedRoutePoints.size() - 1);

        // Zoom to that route
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : selectedRoutePoints) builder.include(point);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));

        saveButton.setVisibility(View.VISIBLE);
        navButton.setVisibility(View.VISIBLE);
    
//        navButton.setOnClickListener(v -> {
//            Location myLocation = mMap.getMyLocation();
//            if (myLocation == null) {
//                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            LatLng currentLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
//
//            LatLng routeStart = points.get(0);
//            LatLng routeEnd = points.get(points.size() - 1);
//
//            ArrayList<String> intermediaryLatLings = getIntent().getStringArrayListExtra("intermediates");
//
//            LatLng landmark1 = savedIntermediaryLatLngs.size() > 0 ? savedIntermediaryLatLngs.get(0) : null;
//            LatLng landmark2 = savedIntermediaryLatLngs.size() > 1 ? savedIntermediaryLatLngs.get(1) : null;
//
//
//            Intent intent = new Intent(RouteGeneratorActivity.this, NavigationActivity.class);
//            intent.putParcelableArrayListExtra("routePoints", new ArrayList<>(points));
//            intent.putExtra("userLocation", currentLatLng);
//            intent.putExtra("startPoint", routeStart);
//            intent.putExtra("endPoint", routeEnd);
//            intent.putExtra("landmark1", landmark1);
//            intent.putExtra("landmark2", landmark2);
//            startActivity(intent);
//        });
    }

    private String buildGpxFromRoute(List<LatLng> points, List<LatLng> intermediates) {
        Log.d("gpx", "intermediates " + intermediates.size());
        StringBuilder gpx = new StringBuilder();
        gpx.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        gpx.append("<gpx version=\"1.1\" creator=\"RouteGeneratorApp\">\n");
        gpx.append("  <trk>\n    <trkseg>\n");
        for (LatLng p : points) {
            gpx.append("      <trkpt lat=\"")
                    .append(p.latitude)
                    .append("\" lon=\"")
                    .append(p.longitude)
                    .append("\"/>\n");
        }
        gpx.append("    </trkseg>\n  </trk>\n");
        if (intermediates != null) {
            for (int i = 0; i < intermediates.size(); i++) {
                LatLng m = intermediates.get(i);
                gpx.append("  <wpt lat=\"")
                        .append(m.latitude)
                        .append("\" lon=\"")
                        .append(m.longitude)
                        .append("\"><name>Landmark ")
                        .append(i + 1)
                        .append("</name></wpt>\n");
            }
        }

        gpx.append("</gpx>");
        return gpx.toString();
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

    private void showRouteDetailsPopup(String title, String distance, String duration,
                                       Runnable onSavePressed,
                                       Runnable onNavigatePressed) {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.route_details_popup, null);

        TextView titleView = view.findViewById(R.id.routeTitle);
        TextView distanceView = view.findViewById(R.id.routeDistance);
        TextView durationView = view.findViewById(R.id.routeDuration);

        titleView.setText(title);
        distanceView.setText("Distance: " + distance);
        durationView.setText("Duration: " + duration);

        // Stop Gallery
        LinearLayout galleryContainer = view.findViewById(R.id.stopGalleryContainer);
        galleryContainer.removeAllViews();

        for (int i = 0; i < intermediateLocations.size(); i++) {
            String stopName = intermediateLocations.get(i);

            TextView stopHeader = new TextView(this);
            stopHeader.setText("Stop " + (i + 1) + ": " + stopName);
            stopHeader.setTextSize(16f);
            stopHeader.setTextColor(Color.BLACK);
            stopHeader.setPadding(0, 16, 0, 8);
            stopHeader.setTypeface(null, Typeface.BOLD);
            galleryContainer.addView(stopHeader);

            ViewPager2 gallery = new ViewPager2(this);
            gallery.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    200
            ));

            List<Integer> galleryImages = new ArrayList<>();
            if (i == 0) {
                galleryImages.add(landmarkImageMap.getOrDefault("CRCE 1", landmarkImageMap.get("default")));
                galleryImages.add(landmarkImageMap.getOrDefault("CRCE 2", landmarkImageMap.get("default")));
            } else {
                galleryImages.add(landmarkImageMap.getOrDefault("ARC 1", landmarkImageMap.get("default")));
                galleryImages.add(landmarkImageMap.getOrDefault("ARC 2", landmarkImageMap.get("default")));
            }

            gallery.setAdapter(new ImageGalleryAdapter(this, galleryImages));
            galleryContainer.addView(gallery);
        }

        popupSaveButton = view.findViewById(R.id.popupSaveButton);
        popupNavButton = view.findViewById(R.id.popupNavButton);
        popupExportButton = view.findViewById(R.id.popupExportButton);

        popupSaveButton.setOnClickListener(v -> {
            dialog.dismiss();
            onSavePressed.run();
        });

        popupNavButton.setOnClickListener(v -> {
            dialog.dismiss();
            onNavigatePressed.run();  // << CORRECT BEHAVIOR
        });

        popupExportButton.setOnClickListener(v -> {
            try {
                String gpxData = buildGpxFromRoute(
                        selectedRoutePoints,
                        savedIntermediaryLatLngs
                );

                String fileName = "route_export_" + System.currentTimeMillis() + ".gpx";
                File gpxFile = new File(getExternalFilesDir(null), fileName);

                FileOutputStream fos = new FileOutputStream(gpxFile);
                fos.write(gpxData.getBytes());
                fos.close();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/gpx+xml");
                shareIntent.putExtra(Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".fileprovider",
                                gpxFile
                        )
                );
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, "Export GPX"));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error exporting GPX", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }


}

