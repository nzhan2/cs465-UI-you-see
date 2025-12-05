package com.example.cs465;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ArrayList<LatLng> routePoints;
    private LatLng startPoint;
    private LatLng endPoint;

    private ArrayList<LatLng> stopPoints;
    private ArrayList<String> stopNames;

    private ImageButton btnZoomIn, btnZoomOut;
    private MaterialButton toggleDirectionsBtn;

    private View bubble, darkOverlay;
    private RecyclerView recyclerDirections;
    private NavigationStepsAdapter adapter;

    private Handler stepHandler = new Handler(Looper.getMainLooper());
    private int currentStep = 0;

    private Marker mockLocationMarker;
    private Handler mockHandler = new Handler(Looper.getMainLooper());
    private int mockIndex = 0;
    private boolean isMockRunning = false;

    private int routeColor = Color.BLUE;

    List<String> hardSteps = Arrays.asList(
            "Illini Union — Head east (39 ft)",
            "Turn right (26 ft)",
            "Walk straight (371 ft)",
            "Head south (62 ft)",
            "Turn left (33 ft)",
            "Turn right (489 ft)",
            "Slight left toward S Mathews Ave (269 ft)",
            "Turn left toward S Mathews Ave (351 ft)",
            "Turn right onto S Mathews Ave (377 ft)",
            "Turn left toward S Goodwin Ave (453 ft)",
            "Turn right onto S Goodwin Ave (266 ft)",
            "Turn left onto W Gregory Dr — CRCE on left (0.1 mi)",
            "Arrive: CRCE",
            "Head west on W Gregory Dr (0.1 mi)",
            "Turn left — stairs (89 ft)",
            "Walk straight (194 ft)",
            "Head south (20 ft)",
            "Turn right (20 ft)",
            "Slight left (479 ft)",
            "Turn right (23 ft)",
            "Turn left (69 ft)",
            "Slight right (371 ft)",
            "Turn left toward E Peabody Dr (39 ft)",
            "Continue (233 ft)",
            "Head south (102 ft)",
            "Turn right onto E Peabody Dr — ARC on left (0.4 mi)",
            "Arrive: ARC",
            "Head east on E Peabody Dr (0.1 mi)",
            "Turn left (249 ft)",
            "Turn right (0.2 mi)",
            "Turn left onto S 6th St (249 ft)",
            "Turn right (423 ft)",
            "Slight left toward W Gregory Dr (13 ft)",
            "Turn right toward W Gregory Dr (236 ft)",
            "Turn left toward W Gregory Dr (33 ft)",
            "Turn right onto W Gregory Dr (148 ft)",
            "Turn left (469 ft)",
            "Turn right (115 ft)",
            "Turn left — Foellinger Auditorium on right",
            "Arrive: Foellinger Auditorium"
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        routePoints = getIntent().getParcelableArrayListExtra("routePoints");
        startPoint = getIntent().getParcelableExtra("startPoint");
        endPoint = getIntent().getParcelableExtra("endPoint");
        stopPoints = getIntent().getParcelableArrayListExtra("stopPoints");
        stopNames = getIntent().getStringArrayListExtra("stopNames");
        routeColor = getIntent().getIntExtra("routeColor", Color.BLUE);

        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        toggleDirectionsBtn = findViewById(R.id.toggleDirectionsBtn);

        bubble = findViewById(R.id.directionsBubble);
        darkOverlay = findViewById(R.id.darkOverlay);
        recyclerDirections = findViewById(R.id.recyclerDirections);

        bubble.setVisibility(View.GONE);
        darkOverlay.setVisibility(View.GONE);

        recyclerDirections.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NavigationStepsAdapter(hardSteps);
        recyclerDirections.setAdapter(adapter);

        toggleDirectionsBtn.setOnClickListener(v -> toggleBubble());

        darkOverlay.setOnClickListener(v -> hideBubble());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapNav);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        Button back = findViewById(R.id.backButtonNav);
        back.setOnClickListener(v -> finish());
    }


    private void toggleBubble() {
        if (bubble.getVisibility() == View.VISIBLE) hideBubble();
        else showBubble();
    }

    private void showBubble() {
        bubble.setVisibility(View.VISIBLE);
        darkOverlay.setVisibility(View.VISIBLE);
        startStepScrolling();
    }

    private void hideBubble() {
        bubble.setVisibility(View.GONE);
        darkOverlay.setVisibility(View.GONE);
    }


    private void startStepScrolling() {
        currentStep = 0;

        stepHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (currentStep >= hardSteps.size())
                    return;

                // Update adapter to show new 3-step window
                adapter.setCurrentIndex(currentStep);

                currentStep++;

                stepHandler.postDelayed(this, 3000);
            }
        }, 1500);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings ui = mMap.getUiSettings();
        ui.setZoomControlsEnabled(false);
        ui.setZoomGesturesEnabled(true);

        btnZoomIn.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomIn()));
        btnZoomOut.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomOut()));

        if (routePoints != null && !routePoints.isEmpty()) {
            mMap.addPolyline(new PolylineOptions()
                    .addAll(routePoints)
                    .color(routeColor)
                    .width(12f));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng p : routePoints) builder.include(p);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        }

        routePoints = new ArrayList<>(generateEvenlySpacedPoints(routePoints, 3));
        startMockLocationSimulation();

        if (stopPoints != null && stopNames != null) {
            for (int i = 0; i < stopPoints.size(); i++) {
                mMap.addMarker(new MarkerOptions()
                        .position(stopPoints.get(i))
                        .title("Stop " + (i + 1) + ": " + stopNames.get(i))
                        .icon(createNumberedMarker(this, i + 1, "#0f69fa")));
            }
        }

        if (startPoint != null)
            mMap.addMarker(new MarkerOptions().position(startPoint).title("Start"));

        if (endPoint != null)
            mMap.addMarker(new MarkerOptions().position(endPoint).title("End"));
    }


    private void startMockLocationSimulation() {
        if (routePoints == null || routePoints.isEmpty() || mMap == null) return;

        BitmapDescriptor dot = createBlueDot();
        mockLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(routePoints.get(0))
                .icon(dot)
                .anchor(0.5f, 0.5f));

        mockIndex = 0;
        isMockRunning = true;

        mockHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isMockRunning || mockIndex >= routePoints.size()) return;

                LatLng next = routePoints.get(mockIndex);
                mockLocationMarker.setPosition(next);
                mockHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isMockRunning || mockIndex >= routePoints.size()) return;

                        LatLng next = routePoints.get(mockIndex);

                        // Move marker only — NO camera movement
                        mockLocationMarker.setPosition(next);

                        mockIndex++;
                        mockHandler.postDelayed(this, 400);
                    }
                });


                mockIndex++;
                mockHandler.postDelayed(this, 400);
            }
        });
    }


    private BitmapDescriptor createBlueDot() {
        int size = 50;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint outer = new Paint(Paint.ANTI_ALIAS_FLAG);
        outer.setColor(Color.parseColor("#4285F4"));
        canvas.drawCircle(size / 2f, size / 2f, size / 2.2f, outer);

        Paint inner = new Paint(Paint.ANTI_ALIAS_FLAG);
        inner.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 4f, inner);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private List<LatLng> generateEvenlySpacedPoints(List<LatLng> points, double step) {
        List<LatLng> spaced = new ArrayList<>();
        if (points == null || points.size() < 2) return spaced;

        spaced.add(points.get(0));
        LatLng last = points.get(0);

        for (int i = 1; i < points.size(); i++) {
            LatLng cur = points.get(i);
            double dist = distanceBetween(last, cur);
            if (dist == 0) continue;

            double steps = dist / step;
            double latStep = (cur.latitude - last.latitude) / steps;
            double lngStep = (cur.longitude - last.longitude) / steps;

            for (int k = 1; k <= steps; k++) {
                spaced.add(new LatLng(
                        last.latitude + latStep * k,
                        last.longitude + lngStep * k
                ));
            }

            last = cur;
        }

        return spaced;
    }



    private double distanceBetween(LatLng a, LatLng b) {
        double R = 6371000;
        double dLat = Math.toRadians(b.latitude - a.latitude);
        double dLng = Math.toRadians(b.longitude - a.longitude);
        double lat1 = Math.toRadians(a.latitude);
        double lat2 = Math.toRadians(b.latitude);

        double h = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.sin(dLng/2) * Math.sin(dLng/2)
                * Math.cos(lat1) * Math.cos(lat2);

        return 2 * R * Math.asin(Math.sqrt(h));
    }



    private BitmapDescriptor createNumberedMarker(Context context, int number, String hexColor) {
        int size = 100;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor(hexColor));

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50f);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawCircle(size / 2f, size / 2f, size / 2.3f, circlePaint);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = size / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(String.valueOf(number), size / 2f, textY, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


}
