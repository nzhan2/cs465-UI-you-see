package com.example.cs465;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Start route generation immediately
        Button generateButton = findViewById(R.id.generateButton);
        generateButton.setOnClickListener(v ->
                startActivity(new Intent(this, RouteSelectorActivity.class))
        );

        // History button not fully functional with real route data
        LinearLayout historySection = findViewById(R.id.historySection);
        historySection.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );

        LinearLayout savedSection = findViewById(R.id.savedSection);
        savedSection.setOnClickListener(v ->
                startActivity(new Intent(this, SavedActivity.class))
        );
    }
}
