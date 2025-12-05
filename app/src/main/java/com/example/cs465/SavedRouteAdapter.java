package com.example.cs465;

import static android.content.Context.MODE_PRIVATE;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SavedRouteAdapter extends RecyclerView.Adapter<SavedRouteAdapter.ViewHolder> {

    private final ArrayList<SavedRoute> routes;
    private final Context context;

    public SavedRouteAdapter(ArrayList<SavedRoute> routes, Context context) {
        this.routes = routes;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.saved_route_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavedRoute route = routes.get(position);

        holder.routeName.setText(route.name != null ? route.name : "Route " + (position + 1));
//        holder.routeInfo.setText("Start → End: " + route.start.latitude + "," + route.start.longitude);
        SharedPreferences prefs = context.getSharedPreferences("routes", MODE_PRIVATE);
        long timestamp = prefs.getLong("last_saved_timestamp_" + position, 0L); // fallback to 0
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        holder.routeInfo.setText(sdf.format(date));

        holder.exportButton.setOnClickListener(v -> {
            exportGpx(route);
        });

        holder.navigateButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, NavigationActivity.class);
            intent.putParcelableArrayListExtra("routePoints", new ArrayList<>(route.polylinePoints));
            intent.putExtra("userLocation", route.start); // or fetch saved user location if you have it
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            routes.remove(position);
            notifyDataSetChanged();

//            SharedPreferences prefs = context.getSharedPreferences("routes", MODE_PRIVATE);
            Gson gson = new Gson();
            prefs.edit().putString("saved_routes_list", gson.toJson(routes)).apply();
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
//        public WindowDecorActionBar.TabImpl routeInfoTextView;
        TextView routeName, routeInfo;
        Button exportButton, navigateButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            routeName = itemView.findViewById(R.id.routeName);
            routeInfo = itemView.findViewById(R.id.routeInfo);
            exportButton = itemView.findViewById(R.id.exportButton);
            navigateButton = itemView.findViewById(R.id.navigateButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private String buildGpx(SavedRoute route) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"CS465-App\">\n");
        sb.append("    <trk>\n");
        sb.append("        <name>").append(route.name != null ? route.name : "Route").append("</name>\n");
        sb.append("        <trkseg>\n");

        for (LatLng pt : route.polylinePoints) {
            sb.append("      <trkpt lat=\"")
                    .append(pt.latitude)
                    .append("\" lon=\"")
                    .append(pt.longitude)
                    .append("\">\n");
            sb.append("      </trkpt>\n");
        }
        sb.append("    </trkseg>\n");
        sb.append("  </trk>\n");
        sb.append("</gpx>");

        return sb.toString();
    }

    private void exportGpx(SavedRoute route) {
        try {
            String gpxData = buildGpx(route);

            String fileName = "route_export_" + route.name + ".gpx";
            File gpxFile = new File(context.getExternalFilesDir(null), fileName);

            FileOutputStream fos = new FileOutputStream(gpxFile);
            fos.write(gpxData.getBytes());
            fos.close();

            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    gpxFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/gpx+xml");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "Export GPX"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

