package com.example.cs465;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.util.List;

public class RouteSelectorActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    String apiKey;

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

        String originName = "Illini Union";
        String destinationName = "Foellinger Auditorium";

        Log.d("RouteSelectorActivity", "API KEY: " + apiKey);

        RouteGeneratorActivityHelper.geocodePlace(originName, apiKey, originLatLng -> {
            RouteGeneratorActivityHelper.geocodePlace(destinationName, apiKey, destLatLng -> {
                Log.d("RouteSelectorActivity", "test");
                RouteGeneratorActivityHelper.getRoutes(originLatLng, destLatLng, apiKey, routes -> {
                    for (int i = 0; i < routes.size(); i++) {
                        List<LatLng> path = routes.get(i);
                        int color = (i == 0) ? Color.BLUE : Color.GRAY;

                        mMap.addPolyline(new PolylineOptions().addAll(path).color(color).width(10f));
                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng point: routes.get(0)) {
                        builder.include(point);
                    }

                    LatLngBounds bounds = builder.build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                });
            });
        });
    }
}
