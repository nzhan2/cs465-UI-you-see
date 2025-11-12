package com.example.cs465;

import static android.graphics.Color.argb;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

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
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteSelectorActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    String apiKey;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);

        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(20.0f);

        String originName = "Illini Union";
        String destinationName = "Foellinger Auditorium";
        List<String> intermediaries = Arrays.asList("Bread Company, Urbana", "ARC, Champaign", "CRCE");

        Log.d("RouteSelectorActivity", "API KEY: " + apiKey);

        RouteGeneratorActivityHelper.geocodePlace(originName, apiKey, originLatLng -> {
            RouteGeneratorActivityHelper.geocodePlace(destinationName, apiKey, destLatLng -> {
                geocodeAllPlaces(intermediaries, apiKey, intermediaryLatLngs -> {
                    RouteGeneratorActivityHelper.getRoutes(originLatLng, destLatLng, intermediaryLatLngs, apiKey, routes -> {
                        for (int i = 0; i < routes.size(); i++) {
                            List<LatLng> path = routes.get(i);

                            int color = routeColors[i % routeColors.length];

                            mMap.addPolyline(new PolylineOptions().addAll(path).color(color).width(10f));
                            mMap.addMarker(new MarkerOptions()
                                    .position(originLatLng)
                                    .title("Start")
                            );
                        }

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng point: routes.get(0)) {
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
    }

    private void geocodeAllPlaces(List<String> placeNames, String apiKey, OnAllGeocodedListener listener) {
        List<LatLng> results = new ArrayList<>();

        if (placeNames.isEmpty()) {
            listener.onAllGeocoded(results);
            return;
        }

        geocodeNextPlace(0, placeNames, results, apiKey, listener);
    }

    private void geocodeNextPlace(int index, List<String> placeNames, List<LatLng> results, String apiKey, OnAllGeocodedListener listener) {
        if (index >= placeNames.size()) {
            listener.onAllGeocoded(results);
            return;
        }

        String name = placeNames.get(index);
        RouteGeneratorActivityHelper.geocodePlace(name, apiKey, latLng -> {
            results.add(latLng);
            geocodeNextPlace(index + 1, placeNames, results, apiKey, listener);
        });
    }
}
