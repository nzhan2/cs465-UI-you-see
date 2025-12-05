package com.example.cs465;

import static java.util.Objects.isNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //history dummy test
//        HistoryStorage.saveRoute(
//                this,
//                new RouteHistoryItem(
//                        "Illini Union",
//                        "Foellinger Auditorium",
//                        System.currentTimeMillis()
//                )
//        );

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Code for route input
        //String[] locations = new String[] {"Illini Union", "Foellinger Auditorium", "ARC", "Altgeld", "CRCE"};

        EditText startEdit = findViewById(R.id.startEditText);
        EditText endEdit = findViewById(R.id.endEditText);
        EditText landmarksEditText = findViewById(R.id.landmarksEditText);

        // ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        //         android.R.layout.simple_dropdown_item_1line, locations);

       /* startEdit.setAdapter(adapter);
        endEdit.setAdapter(adapter);
        landmarksEditText.setAdapter(adapter);*/

        /*startEdit.setThreshold(1);
        endEdit.setThreshold(1);
        landmarksEditText.setThreshold(1);*/

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("from_history", false)) {
            String startFromHistory = intent.getStringExtra("start");
            String endFromHistory = intent.getStringExtra("end");

            if (startFromHistory != null) {
                startEdit.setText(startFromHistory);
            }
            if (endFromHistory != null) {
                endEdit.setText(endFromHistory);
            }
        }

        Button generateButton = findViewById(R.id.generateButton);
        RadioButton timeRadio = findViewById(R.id.timeRadio);
        RadioButton distanceRadio = findViewById(R.id.distanceRadio);
        TextView longRouteText = findViewById(R.id.longRouteText);

        timeRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                longRouteText.setText("min long route");
            }
        });

        distanceRadio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                longRouteText.setText("km long route");
            }
        });

        EditText distanceEdit = findViewById(R.id.constraintValueInput);

        generateButton.setOnClickListener(v -> {
//update generate button to build history when clicked
            String start = startEdit.getText().toString().trim();
            String end   = endEdit.getText().toString().trim();
            String rawLandmarks = landmarksEditText.getText().toString().trim();
            String value = distanceEdit.getText().toString().trim();

            if (start.isEmpty() || end.isEmpty()) {
                Toast.makeText(MainActivity.this,
                        "Please enter both start and end locations",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (value.isEmpty()) {
                Toast.makeText(MainActivity.this,
                        "Please enter a time/distance constraint",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            RouteHistoryItem historyItem = new RouteHistoryItem(
                    start,
                    end,
                    System.currentTimeMillis()
            );
            HistoryStorage.saveRoute(MainActivity.this, historyItem);

            List<String> intermediates = new ArrayList<>();
            if (!rawLandmarks.isEmpty()) {
                intermediates = Arrays.asList(rawLandmarks.split("\\s*,\\s*"));
            }

            Intent buttonintent = new Intent(MainActivity.this, RouteGeneratorActivity.class);

            String measure = (timeRadio.isChecked()) ? "time" : "distance";

            buttonintent.putExtra("start", start);
            buttonintent.putStringArrayListExtra("intermediates", new ArrayList<>(intermediates));
            buttonintent.putExtra("end", end);
            buttonintent.putExtra("measure", measure);
            buttonintent.putExtra("value", value);

            startActivity(buttonintent);
        });

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
