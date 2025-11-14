package com.example.cs465;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load initial data
        List<RouteHistoryItem> routes = HistoryStorage.getRoutes(this);
        adapter = new HistoryAdapter(routes);
        recyclerView.setAdapter(adapter);

        // CLEAR BUTTON
        Button clearButton = findViewById(R.id.clearHistoryButton);
        clearButton.setOnClickListener(v -> {
            HistoryStorage.clearRoutes(this);
            adapter.updateData(HistoryStorage.getRoutes(this));
        });
    }
}
