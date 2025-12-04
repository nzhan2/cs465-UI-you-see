package com.example.cs465;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    private ArrayList<LatLng> routePoints;
    private LatLng userLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        routePoints = getIntent().getParcelableArrayListExtra("routePoints");
        userLocation = getIntent().getParcelableExtra("userLocation");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navMap);
        mapFragment.getMapAsync(this);

        Button backButton = findViewById(R.id.navBackButton);
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        // Enable user location
        enableMyLocation();

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        if (routePoints == null || routePoints.isEmpty() || userLocation == null) {
            Toast.makeText(this, "Missing route or user location", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng routeStart = routePoints.get(0);

        // Draw full route (blue)
        mMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .color(0xFF0000FF)
                .width(12f));

        // Draw connector from current location to start (red)
        mMap.addPolyline(new PolylineOptions()
                .add(userLocation, routeStart)
                .color(0xFFFF0000)
                .width(12f));

        // Zoom to include route + user location
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        bounds.include(userLocation);
        for (LatLng p : routePoints) {
            bounds.include(p);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150));
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

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false; // default behavior also occurs
    }

    @Override
    public void onMyLocationClick(Location location) {
        Toast.makeText(this, "Current location:\nLat: " + location.getLatitude() + ", Lng: " + location.getLongitude(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

//package com.example.cs465;
//
//import android.graphics.Color;
//import android.location.Location;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.fragment.app.FragmentActivity;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.LatLngBounds;
//import com.google.android.gms.maps.model.PolylineOptions;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback {
//
//    private GoogleMap mMap;
//    private ArrayList<LatLng> routePoints;
//    private LatLng userLocation;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_navigation);
//
//
//        // Retrieve route points and user location from intent
//        routePoints = getIntent().getParcelableArrayListExtra("routePoints");
//        userLocation = getIntent().getParcelableExtra("userLocation");
//
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.navMap);
//        mapFragment.getMapAsync(this);
//
//        Button backButton = findViewById(R.id.navBackButton);
//        backButton.setOnClickListener(v -> {
//            finish(); // returns to MainActivity (the previous screen)
//        });
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        if (routePoints == null || routePoints.isEmpty() || userLocation == null) {
//            Toast.makeText(this, "Missing route or user location", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        LatLng routeStart = routePoints.get(0);
//
//        // Draw the full route (BLUE)
//        mMap.addPolyline(new PolylineOptions()
//                .addAll(routePoints)
//                .color(Color.parseColor("#0000FF"))   // blue
//                .width(12f));
//
//        // Draw the connector from user → route start (RED)
//        mMap.addPolyline(new PolylineOptions()
//                .add(userLocation, routeStart)
//                .color(Color.parseColor("#FF0000"))   // red
//                .width(12f));
//
////        LatLng startPoint = getIntent().getParcelableExtra("startPoint");
////        LatLng endPoint = getIntent().getParcelableExtra("endPoint");
////        LatLng landmark1 = getIntent().getParcelableExtra("landmark1");
////        LatLng landmark2 = getIntent().getParcelableExtra("landmark2");
////
////        List<LatLng> segmentCurrentToStart = new ArrayList<>();
////        List<LatLng> segmentStartToL1 = new ArrayList<>();
////        List<LatLng> segmentL1ToL2 = new ArrayList<>();
////        List<LatLng> segmentL2ToEnd = new ArrayList<>();
////
////        boolean reachedStart = false;
////        boolean reachedL1 = (landmark1 == null);
////        boolean reachedL2 = (landmark2 == null);
////
////        for (LatLng p : routePoints) {
////
////            if (!reachedStart) {
////                segmentCurrentToStart.add(p);
////
////                if (isSamePoint(p, startPoint)) {
////                    reachedStart = true;
////                }
////                continue;
////            }
////
////            if (!reachedL1) {
////                segmentStartToL1.add(p);
////
////                if (isSamePoint(p, landmark1)) {
////                    reachedL1 = true;
////                }
////                continue;
////            }
////
////            if (!reachedL2) {
////                segmentL1ToL2.add(p);
////
////                if (isSamePoint(p, landmark2)) {
////                    reachedL2 = true;
////                }
////                continue;
////            }
////
////            // After L2 → end
////            segmentL2ToEnd.add(p);
////        }
////
////
////        mMap.addPolyline(new PolylineOptions()
////                .addAll(segmentCurrentToStart)
////                .width(12)
////                .color(Color.parseColor("#FF0000")));   // red
////
////        mMap.addPolyline(new PolylineOptions()
////                .addAll(segmentStartToL1)
////                .width(12)
////                .color(Color.parseColor("#FFA500")));   // orange
////
////        mMap.addPolyline(new PolylineOptions()
////                .addAll(segmentL1ToL2)
////                .width(12)
////                .color(Color.parseColor("#00FF00")));   // green
////
////        mMap.addPolyline(new PolylineOptions()
////                .addAll(segmentL2ToEnd)
////                .width(12)
////                .color(Color.parseColor("#0000FF")));   // blue
//
//
//
////        List<LatLng> segmentCurrentToStart = new ArrayList<>();
////        List<LatLng> segmentStartToL1 = new ArrayList<>();
////        List<LatLng> segmentL1ToL2 = new ArrayList<>();
////        List<LatLng> segmentL2ToEnd = new ArrayList<>();
////
////        boolean reachedStart = false;
////        boolean reachedL1 = (landmark1 == null);
////        boolean reachedL2 = (landmark2 == null);
////
////        for (LatLng p : routePoints) {
////
////            if (!reachedStart) {
////                segmentCurrentToStart.add(p);
////                if (p.equals(startPoint)) reachedStart = true;
////                continue;
////            }
////
////            if (!reachedL1) {
////                segmentStartToL1.add(p);
////                if (p.equals(landmark1)) reachedL1 = true;
////                continue;
////            }
////
////            if (!reachedL2) {
////                segmentL1ToL2.add(p);
////                if (p.equals(landmark2)) reachedL2 = true;
////                continue;
////            }
////
////            segmentL2ToEnd.add(p);
////        }
////
////
////// RED: user → start
////        mMap.addPolyline(new PolylineOptions()
////                .add(userLocation, startPoint)
////                .color(0xFFFF0000)
////                .width(12f));
////
////// ORANGE: start → landmark1
////        if (landmark1 != null) {
////            mMap.addPolyline(new PolylineOptions()
////                    .add(startPoint, landmark1)
////                    .color(0xFFFFA500)  // orange
////                    .width(12f));
////        }
////
////// GREEN: landmark1 → landmark2
////        if (landmark1 != null && landmark2 != null) {
////            mMap.addPolyline(new PolylineOptions()
////                    .add(landmark1, landmark2)
////                    .color(0xFF00FF00)
////                    .width(12f));
////        }
////
////// BLUE: landmark2 → end
////        mMap.addPolyline(new PolylineOptions()
////                .add(landmark2 != null ? landmark2 : startPoint, endPoint)
////                .color(0xFF0000FF)
////                .width(12f));
//
////        // Draw the route
////        mMap.addPolyline(new PolylineOptions()
////                .addAll(routePoints)
////                .color(0xFF0000FF) // blue
////                .width(20f));
////
////        // Draw connector line from user → route start
////        mMap.addPolyline(new PolylineOptions()
////                .add(userLocation, routePoints.get(0))
////                .color(0xFFFF0000) // red
////                .width(12f));
//
//        // Zoom to fit both
//        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
//        boundsBuilder.include(userLocation);
//        for (LatLng point : routePoints) boundsBuilder.include(point);
//
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150));
//    }
////    private boolean isSamePoint(LatLng a, LatLng b) {
////        if (a == null || b == null) return false;
////        return Math.abs(a.latitude - b.latitude) < 0.00001 &&
////                Math.abs(a.longitude - b.longitude) < 0.00001;
////    }
//
//}
