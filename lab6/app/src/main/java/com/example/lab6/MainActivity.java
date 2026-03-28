package com.example.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        EditText editTextCity = findViewById(R.id.editTextCity);
        Button buttonGetWeather = findViewById(R.id.buttonGetWeather);

        buttonGetWeather.setOnClickListener(v -> {
            String city = editTextCity.getText().toString().trim();
            if (!city.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                intent.putExtra("city", city);
                startActivity(intent);
            } else {
                editTextCity.setError("Please enter city name");
            }
        });
    }
}