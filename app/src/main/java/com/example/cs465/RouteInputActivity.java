package com.example.cs465;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RouteInputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_input);

        String[] locations = new String[] {"Illini Union", "Foellinger Auditorium", "ARC", "Altgeld", "CRCE"};

        AutoCompleteTextView startEdit = findViewById(R.id.startEditText);
        AutoCompleteTextView endEdit = findViewById(R.id.endEditText);
        AutoCompleteTextView landmarksEditText = findViewById(R.id.landmarksEditText);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, locations);

        startEdit.setAdapter(adapter);
        endEdit.setAdapter(adapter);
        landmarksEditText.setAdapter(adapter);

        // Optional: show suggestions after typing 1 character
        startEdit.setThreshold(1);
        endEdit.setThreshold(1);
        landmarksEditText.setThreshold(1);

        Button generateButton = findViewById(R.id.generateButton);
        RadioButton timeRadio = findViewById(R.id.timeRadio);
        RadioButton distanceRadio = findViewById(R.id.distanceRadio);
        EditText distanceEdit = findViewById(R.id.distanceEditText);

        generateButton.setOnClickListener(v -> {
            String start = startEdit.getText().toString().trim();
            String end = endEdit.getText().toString().trim();
            List<String> intermediates = Arrays.asList(
                    landmarksEditText.getText().toString().trim().split("\\s*,\\s*"));

            String measure = (timeRadio.isChecked()) ? "time" : "distance";
            String distance = distanceEdit.getText().toString().trim();

            Intent intent = new Intent(RouteInputActivity.this, RouteSelectorActivity.class);
            intent.putExtra("start", start);
            intent.putStringArrayListExtra("intermediates", new ArrayList<>(intermediates));
            intent.putExtra("end", end);
            intent.putExtra("measure", measure);
            intent.putExtra("distance", distance);
            startActivity(intent);
        });
    }
}
