package com.example.cs465;

import android.content.Intent;       // ✅ Import for navigation
import android.os.Bundle;
import android.widget.Button;        // ✅ Import for the Button view

import androidx.appcompat.app.AppCompatActivity;

public class SavedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v ->
                startActivity(new Intent(SavedActivity.this, MainActivity.class))
        );
    }
}
