package com.example.cs465;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NavigationStepsAdapter extends RecyclerView.Adapter<NavigationStepsAdapter.StepViewHolder> {

    private List<String> allSteps;   // entire steps list
    private int currentIndex = 0;    // index of currently active step

    public NavigationStepsAdapter(List<String> steps) {
        this.allSteps = steps;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
        notifyDataSetChanged();
    }

    /** Return ONLY 3 visible items: previous, current, next */
    @Override
    public int getItemCount() {
        return 3;
    }

    /** Map each visible row to actual index */
    private int getMappedIndex(int position) {
        // position 0 = previous step
        // position 1 = current step
        // position 2 = next step
        return Math.max(0, Math.min(allSteps.size() - 1, currentIndex + (position - 1)));
    }

    @Override
    public void onBindViewHolder(StepViewHolder holder, int position) {
        int realIndex = getMappedIndex(position);
        String step = allSteps.get(realIndex);

        holder.stepText.setText(step);

        if (position == 1) {
            holder.stepText.setTextColor(Color.parseColor("#0f69fa"));  // Blue highlight
        } else {
            holder.stepText.setTextColor(Color.BLACK);
        }
    }

    @Override
    public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_navigation_step, parent, false);
        return new StepViewHolder(view);
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepText;

        StepViewHolder(View itemView) {
            super(itemView);
            stepText = itemView.findViewById(R.id.stepText);
        }
    }
}
