package com.example.cs465;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NavigationStepsAdapter extends RecyclerView.Adapter<NavigationStepsAdapter.ViewHolder> {

    private final List<String> steps;

    public NavigationStepsAdapter(List<String> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_navigation_step, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.stepText.setText(steps.get(position));
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stepText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            stepText = itemView.findViewById(R.id.stepText);
        }
    }
}
