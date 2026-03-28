package com.example.lab5;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        ListView listViewHistory = findViewById(R.id.listViewHistory);
        Button buttonClearHistory = findViewById(R.id.buttonClearHistory);

        loadHistory(listViewHistory);

        buttonClearHistory.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("level_history", MODE_PRIVATE);
            prefs.edit().remove("history").apply();
            loadHistory(listViewHistory);
        });
    }

    private void loadHistory(ListView listViewHistory) {
        SharedPreferences prefs = getSharedPreferences("level_history", MODE_PRIVATE);
        String json = prefs.getString("history", "");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<MeasurementHistory>>() {}.getType();
        List<MeasurementHistory> historyList = gson.fromJson(json, type);

        List<String> historyDisplayList = new ArrayList<>();

        if (historyList != null && !historyList.isEmpty()) {
            for (MeasurementHistory item : historyList) {
                String display = String.format(Locale.getDefault(),
                        "%s\nКути: X=%.1f°, Y=%.1f°, Z=%.1f°\n%s",
                        item.date, item.angleX, item.angleY, item.angleZ, item.status);
                historyDisplayList.add(display);
            }
        } else {
            historyDisplayList.add(getString(R.string.history_empty));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyDisplayList);
        listViewHistory.setAdapter(adapter);
    }
}