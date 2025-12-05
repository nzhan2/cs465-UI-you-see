package com.example.cs465;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.content.Intent;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final List<RouteHistoryItem> routes;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
    private final Context context;

    public HistoryAdapter(List<RouteHistoryItem> routes, Context context) {

        this.routes = routes;
        this.context = context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeText, dateText;
        Button viewRouteButton;
        ViewHolder(View view) {
            super(view);
            routeText = view.findViewById(R.id.routeText);
            dateText = view.findViewById(R.id.dateText);
            viewRouteButton = view.findViewById(R.id.viewRouteButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RouteHistoryItem item = routes.get(position);
        holder.routeText.setText(item.getOrigin() + " → " + item.getDestination());
        holder.dateText.setText(sdf.format(new Date(item.getTimestamp())));
        holder.viewRouteButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, NavigationActivity.class);
            intent.putParcelableArrayListExtra("routePoints", new ArrayList<>(item.polylinePoints));
            intent.putExtra("userLocation", item.start);
            context.startActivity(intent);
        });
    }


    public void updateData(List<RouteHistoryItem> newRoutes) {
        this.routes.clear();
        this.routes.addAll(newRoutes);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return routes.size(); }
}
