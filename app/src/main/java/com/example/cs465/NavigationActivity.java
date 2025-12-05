package com.example.cs465;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

    private RecyclerView directionsList;
    List<String> hardSteps = Arrays.asList(
            // ----- Segment 1: Illini Union → CRCE -----
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
            "Turn left onto W Gregory Dr — Campus Recreation Center East on left (0.1 mi)",
            "Arrive: Campus Recreation Center East",

            // ----- Segment 2: CRCE → ARC -----
            "Head west on W Gregory Dr (0.1 mi)",
            "Turn left — Take the stairs (89 ft)",
            "Walk straight (194 ft)",
            "Head south (20 ft)",
            "Turn right (20 ft)",
            "Slight left (479 ft)",
            "Turn right (23 ft)",
            "Turn left (69 ft)",
            "Slight right (371 ft)",
            "Turn left toward E Peabody Dr (39 ft)",
            "Continue (233 ft)",
            "Head south toward E Peabody Dr (102 ft)",
            "Turn right onto E Peabody Dr — ARC on left (0.4 mi)",
            "Arrive: Activities and Recreation Center",

            // ----- Segment 3: ARC → Foellinger -----
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

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private MaterialButton toggleDirectionsBtn;

    private int routeColor = Color.BLUE;

    private ArrayList<LatLng> stopPoints;
    private ArrayList<String> stopNames;

    private ImageButton btnZoomIn;
    private ImageButton btnZoomOut;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Button backButtonNav = findViewById(R.id.backButtonNav);
        backButtonNav.setOnClickListener(v -> finish());




        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);


        routePoints = getIntent().getParcelableArrayListExtra("routePoints");
        startPoint = getIntent().getParcelableExtra("startPoint");
        endPoint = getIntent().getParcelableExtra("endPoint");
        routeColor = getIntent().getIntExtra("routeColor", Color.BLUE);
        stopPoints = getIntent().getParcelableArrayListExtra("stopPoints");
        stopNames = getIntent().getStringArrayListExtra("stopNames");


        toggleDirectionsBtn = findViewById(R.id.toggleDirectionsBtn);
        directionsList = findViewById(R.id.recyclerDirections);

        View bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        toggleDirectionsBtn.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        setupDirectionsList();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapNav);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    private void setupDirectionsList() {
        directionsList.setLayoutManager(new LinearLayoutManager(this));

        NavigationStepsAdapter adapter = new NavigationStepsAdapter(hardSteps);
        directionsList.setAdapter(adapter);

        adapter.notifyDataSetChanged();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable built-in Google Maps zoom buttons
        UiSettings ui = mMap.getUiSettings();


        ui.setZoomControlsEnabled(false);
        ui.setZoomGesturesEnabled(true);

        btnZoomIn.setOnClickListener(v ->
                mMap.animateCamera(CameraUpdateFactory.zoomIn()));

        btnZoomOut.setOnClickListener(v ->
                mMap.animateCamera(CameraUpdateFactory.zoomOut()));


        if (routePoints != null && !routePoints.isEmpty()) {
            PolylineOptions lineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .color(routeColor)

                    .width(12f);

            mMap.addPolyline(lineOptions);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng point : routePoints) builder.include(point);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        }

        // Add numbered stop markers  ← Perfect placement
        if (stopPoints != null && stopNames != null) {
            for (int i = 0; i < stopPoints.size(); i++) {
                LatLng stop = stopPoints.get(i);
                String name = stopNames.get(i);

                mMap.addMarker(new MarkerOptions()
                        .position(stop)
                        .title("Stop " + (i + 1) + ": " + name)
                        .icon(createNumberedMarker(this, i + 1, "#0f69fa"))
                );
            }
        }

        if (startPoint != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(startPoint)
                    .title("Start"));
        }

        if (endPoint != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(endPoint)
                    .title("End"));
        }
    }
}


