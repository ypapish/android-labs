package com.example.lab6;

public class WeatherData {
    private final String cityName;
    private final double temperature;
    private final double feelsLike;
    private final int humidity;
    private final int pressure;
    private final double windSpeed;
    private final String description;
    private final String iconCode;

    public WeatherData(String cityName, double temperature, double feelsLike,
                       int humidity, int pressure, double windSpeed,
                       String description, String iconCode) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.description = description;
        this.iconCode = iconCode;
    }

    public String getCityName() { return cityName; }
    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public int getHumidity() { return humidity; }
    public int getPressure() { return pressure; }
    public double getWindSpeed() { return windSpeed; }
    public String getDescription() { return description; }
}