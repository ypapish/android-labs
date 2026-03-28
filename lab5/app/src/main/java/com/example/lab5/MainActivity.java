package com.example.lab5;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button buttonStartLevel = findViewById(R.id.buttonStartLevel);
        Button buttonHistory = findViewById(R.id.buttonHistory);

        buttonStartLevel.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LevelActivity.class);
            startActivity(intent);
        });

        buttonHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }
}