package com.example.cs465;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final List<RouteHistoryItem> routes;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public HistoryAdapter(List<RouteHistoryItem> routes) {
        this.routes = routes;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView routeText, dateText;
        ViewHolder(View view) {
            super(view);
            routeText = view.findViewById(R.id.routeText);
            dateText = view.findViewById(R.id.dateText);
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
    }


    public void updateData(List<RouteHistoryItem> newRoutes) {
        this.routes.clear();
        this.routes.addAll(newRoutes);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return routes.size(); }
}
