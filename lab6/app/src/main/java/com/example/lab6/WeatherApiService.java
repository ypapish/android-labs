package com.example.lab6;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherApiService {

    private static final String TAG = "WeatherApiService";
    private static final String API_KEY = "b15faa5d136f89da054f69a5e48fd1d0";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface WeatherCallback {
        void onSuccess(WeatherData weatherData);
        void onError(String errorMessage);
    }

    public static void getWeather(String cityName, WeatherCallback callback) {
        executor.execute(() -> {
            String errorMessage = null;
            WeatherData result = null;

            String urlString = String.format(Locale.US,
                    "%s?q=%s&appid=%s&units=metric&lang=en",
                    BASE_URL, cityName, API_KEY);

            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readStream(connection.getInputStream());
                    result = parseWeatherData(response, cityName);
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    errorMessage = "City not found";
                } else if (responseCode == 401) {
                    errorMessage = "Invalid API key. Please wait for activation.";
                } else {
                    errorMessage = "Server error: " + responseCode;
                }
            } catch (IOException e) {
                Log.e(TAG, "Network error", e);
                errorMessage = "Network error. Check your internet connection.";
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error", e);
                errorMessage = "Data processing error";
            }

            final String finalErrorMessage = errorMessage;
            final WeatherData finalResult = result;

            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            mainHandler.post(() -> {
                if (finalResult != null) {
                    callback.onSuccess(finalResult);
                } else {
                    callback.onError(finalErrorMessage);
                }
            });
        });
    }

    private static String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private static WeatherData parseWeatherData(String json, String cityName) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONObject main = root.getJSONObject("main");
        JSONObject wind = root.getJSONObject("wind");

        JSONArray weatherArray = root.optJSONArray("weather");
        JSONObject weather = null;
        if (weatherArray != null && weatherArray.length() > 0) {
            weather = weatherArray.getJSONObject(0);
        }

        double temperature = main.getDouble("temp");
        double feelsLike = main.getDouble("feels_like");
        int humidity = main.getInt("humidity");
        int pressure = main.getInt("pressure");
        double windSpeed = wind.getDouble("speed");

        String description = "";
        String iconCode = "";
        if (weather != null) {
            description = weather.getString("description");
            iconCode = weather.getString("icon");
            if (!description.isEmpty()) {
                description = description.substring(0, 1).toUpperCase() + description.substring(1);
            }
        }

        return new WeatherData(cityName, temperature, feelsLike,
                humidity, pressure, windSpeed, description, iconCode);
    }
}