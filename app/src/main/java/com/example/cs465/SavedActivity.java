package com.example.cs465;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SavedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v ->
                startActivity(new Intent(SavedActivity.this, MainActivity.class))
        );

        // RecyclerView setup
        RecyclerView recyclerView = findViewById(R.id.savedRoutesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load saved routes list
        SharedPreferences prefs = getSharedPreferences("routes", MODE_PRIVATE);
        String json = prefs.getString("saved_routes_list", null);

        ArrayList<SavedRoute> savedRoutes;
        if (json != null) {
            Type listType = new TypeToken<ArrayList<SavedRoute>>(){}.getType();
            savedRoutes = new Gson().fromJson(json, listType);
        } else {
            savedRoutes = new ArrayList<>();
        }

        // Set RecyclerView adapter
        SavedRouteAdapter adapter = new SavedRouteAdapter(savedRoutes, this);
        recyclerView.setAdapter(adapter);


//        SharedPreferences prefs = getSharedPreferences("routes", MODE_PRIVATE);
//        String routeJson = prefs.getString("route_points", null);
//        long timestamp = prefs.getLong("route_timestamp", 0);
//
//        Button navigateButton = findViewById(R.id.navigateButton);
//        navigateButton.setOnClickListener(v -> {
//
////            SharedPreferences prefs = getSharedPreferences("routes", MODE_PRIVATE);
////            String routeJson = prefs.getString("route_points", null);
//            String userJson = prefs.getString("userLocation", null);
//
//            if (routeJson == null || userJson == null) {
//                Toast.makeText(this, "No saved route or user location.", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Convert JSON → List<LatLng>
//            Gson gson = new Gson();
//            Type listType = new TypeToken<ArrayList<LatLng>>(){}.getType();
//            ArrayList<LatLng> routePoints = gson.fromJson(routeJson, listType);
//            LatLng userLocation = gson.fromJson(userJson, LatLng.class);
//
//            Intent intent = new Intent(SavedActivity.this, NavigationActivity.class);
//            intent.putParcelableArrayListExtra("routePoints", routePoints);
//            intent.putExtra("userLocation", userLocation);
//            startActivity(intent);
//        });
//
//        TextView routeName = findViewById(R.id.routeName);
//        TextView routeTimestamp = findViewById(R.id.routeTimestamp);
//
//        if (routeJson != null) {
//            routeName.setText("Saved Route");
//
//            Date date = new Date(timestamp);
//            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd  HH:mm");
//            routeTimestamp.setText(sdf.format(date));
//        } else {
//            routeName.setText("No saved routes");
//            routeTimestamp.setText("");
//        }



    }
}
