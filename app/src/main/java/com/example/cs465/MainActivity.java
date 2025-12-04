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

        // Optional: show suggestions after typing 1 character
        /*startEdit.setThreshold(1);
        endEdit.setThreshold(1);
        landmarksEditText.setThreshold(1);*/

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
            Log.d("debug", "test");
            Log.d("debug", "start: " + startEdit.getText());
            String start = startEdit.getText().toString().trim();
            String end = endEdit.getText().toString().trim();
            List<String> intermediates = Arrays.asList(
                    landmarksEditText.getText().toString().trim().split("\\s*,\\s*"));
            ArrayList<String> locationList = new ArrayList<>();
            locationList.add(start);
            locationList.addAll(intermediates);
            locationList.add(end);

            Intent intent = new Intent(MainActivity.this, RouteGeneratorActivity.class);

            String measure = (timeRadio.isChecked()) ? "time" : "distance";
            String value = distanceEdit.getText().toString().trim();

            intent.putExtra("start", start);
            intent.putStringArrayListExtra("intermediates", new ArrayList<>(intermediates));
            intent.putExtra("end", end);
            intent.putExtra("measure", measure);
            intent.putExtra("value", value);
            startActivity(intent);
        });

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
