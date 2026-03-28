package com.example.lab5;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeasurementHistory {
    public String date;
    public float angleX;
    public float angleY;
    public float angleZ;
    public String status;

    public MeasurementHistory(float angleX, float angleY, float angleZ, String status) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        this.date = sdf.format(new Date());
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;
        this.status = status;
    }
}