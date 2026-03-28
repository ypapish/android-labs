package com.example.lab6;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class WeatherActivity extends AppCompatActivity {

    private TextView textViewCity;
    private TextView textViewTemperature;
    private TextView textViewFeelsLike;
    private TextView textViewHumidity;
    private TextView textViewPressure;
    private TextView textViewWind;
    private TextView textViewDescription;
    private TextView textViewError;
    private ProgressBar progressBar;

    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather);

        initViews();
        getCityFromIntent();
        loadWeather();
    }

    private void initViews() {
        textViewCity = findViewById(R.id.textViewCity);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewFeelsLike = findViewById(R.id.textViewFeelsLike);
        textViewHumidity = findViewById(R.id.textViewHumidity);
        textViewPressure = findViewById(R.id.textViewPressure);
        textViewWind = findViewById(R.id.textViewWind);
        textViewDescription = findViewById(R.id.textViewDescription);
        textViewError = findViewById(R.id.textViewError);
        progressBar = findViewById(R.id.progressBar);
        Button buttonBack = findViewById(R.id.buttonBack);

        buttonBack.setOnClickListener(v -> finish());
    }

    private void getCityFromIntent() {
        cityName = getIntent().getStringExtra("city");
        if (cityName == null || cityName.isEmpty()) {
            finish();
        }
    }

    private void loadWeather() {
        showLoading(true);
        WeatherApiService.getWeather(cityName, new WeatherApiService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherData weatherData) {
                runOnUiThread(() -> {
                    showLoading(false);
                    displayWeather(weatherData);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(errorMessage);
                });
            }
        });
    }

    private void displayWeather(WeatherData weather) {
        textViewCity.setText(String.format(getString(R.string.city_label), weather.getCityName()));
        textViewTemperature.setText(String.format(getString(R.string.temperature), weather.getTemperature()));
        textViewFeelsLike.setText(String.format(getString(R.string.feels_like), weather.getFeelsLike()));
        textViewHumidity.setText(String.format(getString(R.string.humidity), weather.getHumidity()));
        textViewPressure.setText(String.format(getString(R.string.pressure), weather.getPressure()));
        textViewWind.setText(String.format(getString(R.string.wind_speed), weather.getWindSpeed()));
        textViewDescription.setText(String.format(getString(R.string.weather_description), weather.getDescription()));

        textViewError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        textViewError.setText(message);
        textViewError.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}