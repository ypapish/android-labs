package com.example.lab5;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LevelActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewAngleX;
    private TextView textViewAngleY;
    private TextView textViewAngleZ;
    private TextView textViewStatus;
    private TextView textViewAccuracy;
    private View viewBubble;
    private Button buttonResetCalibration;
    private Button buttonToggleVibration;
    private Button buttonSaveHistory;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;

    private float[] gravity = new float[3];
    private final float[] calibrationOffset = new float[2];
    private boolean isCalibrated = false;
    private boolean isVibrationEnabled = true;
    private int frameWidth = 0;
    private int frameHeight = 0;

    private static final float HORIZONTAL_THRESHOLD = 1.0f;
    private static final long VIBRATION_DELAY = 500;
    private static final float MAX_VISUAL_ANGLE = 80.0f;
    private static final String TAG = "LevelActivity";

    private long lastVibrationTime = 0;
    private boolean wasHorizontal = false;

    private List<MeasurementHistory> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level);

        initViews();
        setupSensors();
        loadHistory();

        viewBubble.post(() -> {
            if (viewBubble.getParent() instanceof View) {
                View parent = (View) viewBubble.getParent();
                frameWidth = parent.getWidth();
                frameHeight = parent.getHeight();
            }
        });

        buttonResetCalibration.setOnClickListener(v -> resetCalibration());
        buttonToggleVibration.setOnClickListener(v -> toggleVibration());
        buttonSaveHistory.setOnClickListener(v -> saveCurrentMeasurement());
    }

    private void initViews() {
        textViewAngleX = findViewById(R.id.textViewAngleX);
        textViewAngleY = findViewById(R.id.textViewAngleY);
        textViewAngleZ = findViewById(R.id.textViewAngleZ);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewAccuracy = findViewById(R.id.textViewAccuracy);
        viewBubble = findViewById(R.id.viewBubble);
        buttonResetCalibration = findViewById(R.id.buttonResetCalibration);
        buttonToggleVibration = findViewById(R.id.buttonToggleVibration);
        buttonSaveHistory = findViewById(R.id.buttonSaveHistory);
    }

    private void setupSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                textViewStatus.setText(R.string.sensor_unavailable);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone();
            calculateAngles();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        String accuracyText;
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyText = getString(R.string.accuracy_high);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyText = getString(R.string.accuracy_medium);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyText = getString(R.string.accuracy_low);
                break;
            default:
                accuracyText = getString(R.string.accuracy_unreliable);
                break;
        }
        textViewAccuracy.setText(String.format(getString(R.string.accuracy_label), accuracyText));
    }

    private float safeAsin(float value) {
        float clampedValue = Math.max(-0.999999f, Math.min(0.999999f, value));
        return (float) Math.toDegrees(Math.asin(clampedValue));
    }

    private void calculateAngles() {
        float angleX = safeAsin(gravity[0] / SensorManager.GRAVITY_EARTH);
        float angleY = safeAsin(gravity[1] / SensorManager.GRAVITY_EARTH);

        float rawAngleZ = safeAsin(gravity[2] / SensorManager.GRAVITY_EARTH);
        float angleZ = 90.0f - rawAngleZ;

        float calibratedAngleX = angleX;
        float calibratedAngleY = angleY;

        if (isCalibrated) {
            calibratedAngleX = angleX - calibrationOffset[0];
            calibratedAngleY = angleY - calibrationOffset[1];
        }

        float displayX = Math.round(calibratedAngleX * 10) / 10.0f;
        float displayY = Math.round(calibratedAngleY * 10) / 10.0f;
        float displayZ = Math.round(angleZ * 10) / 10.0f;

        if (displayX == -0.0f) displayX = 0.0f;
        if (displayY == -0.0f) displayY = 0.0f;
        if (displayZ == -0.0f) displayZ = 0.0f;

        textViewAngleX.setText(String.format(Locale.getDefault(), getString(R.string.angle_x), displayX));
        textViewAngleY.setText(String.format(Locale.getDefault(), getString(R.string.angle_y), displayY));
        textViewAngleZ.setText(String.format(Locale.getDefault(), getString(R.string.angle_z), displayZ));

        updateStatus(displayX, displayY);
        updateBubblePosition(calibratedAngleX, calibratedAngleY);
    }

    private void updateStatus(float angleX, float angleY) {
        boolean isXHorizontal = Math.abs(angleX) <= HORIZONTAL_THRESHOLD;
        boolean isYHorizontal = Math.abs(angleY) <= HORIZONTAL_THRESHOLD;
        boolean isHorizontal = isXHorizontal && isYHorizontal;

        String status;
        int statusColor;

        if (isHorizontal) {
            status = getString(R.string.level_horizontal);
            statusColor = ContextCompat.getColor(this, R.color.green);
            if (!wasHorizontal && isVibrationEnabled) {
                triggerVibration();
            }
            wasHorizontal = true;
        } else if (!isXHorizontal && !isYHorizontal) {
            status = getString(R.string.level_tilt_both);
            statusColor = ContextCompat.getColor(this, R.color.red);
            wasHorizontal = false;
        } else if (!isXHorizontal) {
            status = angleX > 0 ? getString(R.string.level_tilt_x) : getString(R.string.level_tilt_x_left);
            statusColor = ContextCompat.getColor(this, R.color.orange);
            wasHorizontal = false;
        } else {
            status = angleY > 0 ? getString(R.string.level_tilt_y) : getString(R.string.level_tilt_y_down);
            statusColor = ContextCompat.getColor(this, R.color.orange);
            wasHorizontal = false;
        }

        textViewStatus.setText(String.format(getString(R.string.level_status), status));
        textViewStatus.setBackgroundColor(statusColor);
    }

    private void updateBubblePosition(float angleX, float angleY) {
        if (viewBubble == null) return;

        if (frameWidth == 0 || frameHeight == 0) {
            viewBubble.post(() -> {
                if (viewBubble.getParent() instanceof View) {
                    View parent = (View) viewBubble.getParent();
                    frameWidth = parent.getWidth();
                    frameHeight = parent.getHeight();
                    updateBubblePosition(angleX, angleY);
                }
            });
            return;
        }

        int bubbleWidth = viewBubble.getWidth();
        int bubbleHeight = viewBubble.getHeight();

        if (bubbleWidth == 0 || bubbleHeight == 0) return;

        int maxOffsetX = (frameWidth - bubbleWidth) / 2;
        int maxOffsetY = (frameHeight - bubbleHeight) / 2;

        if (maxOffsetX <= 0 || maxOffsetY <= 0) return;

        float visualX = Math.max(-MAX_VISUAL_ANGLE, Math.min(MAX_VISUAL_ANGLE, angleX));
        float visualY = Math.max(-MAX_VISUAL_ANGLE, Math.min(MAX_VISUAL_ANGLE, angleY));

        float offsetX = (visualX / MAX_VISUAL_ANGLE) * maxOffsetX;
        float offsetY = (-visualY / MAX_VISUAL_ANGLE) * maxOffsetY;

        offsetX = Math.max(-maxOffsetX, Math.min(maxOffsetX, offsetX));
        offsetY = Math.max(-maxOffsetY, Math.min(maxOffsetY, offsetY));

        viewBubble.setTranslationX(offsetX);
        viewBubble.setTranslationY(offsetY);
    }

    private void triggerVibration() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastVibrationTime > VIBRATION_DELAY && vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(100);
            }
            lastVibrationTime = currentTime;
        }
    }

    private void resetCalibration() {
        isCalibrated = true;
        calibrationOffset[0] = safeAsin(gravity[0] / SensorManager.GRAVITY_EARTH);
        calibrationOffset[1] = safeAsin(gravity[1] / SensorManager.GRAVITY_EARTH);
        android.widget.Toast.makeText(this, R.string.calibrated, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void toggleVibration() {
        isVibrationEnabled = !isVibrationEnabled;
        buttonToggleVibration.setText(isVibrationEnabled ? R.string.vibration_on : R.string.vibration_off);
        android.widget.Toast.makeText(this, isVibrationEnabled ? R.string.vibration_on : R.string.vibration_off, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void saveCurrentMeasurement() {
        float angleX = safeAsin(gravity[0] / SensorManager.GRAVITY_EARTH);
        float angleY = safeAsin(gravity[1] / SensorManager.GRAVITY_EARTH);
        float rawAngleZ = safeAsin(gravity[2] / SensorManager.GRAVITY_EARTH);
        float angleZ = 90.0f - rawAngleZ;

        float calibratedX = angleX;
        float calibratedY = angleY;

        if (isCalibrated) {
            calibratedX = angleX - calibrationOffset[0];
            calibratedY = angleY - calibrationOffset[1];
        }

        calibratedX = Math.round(calibratedX * 10) / 10.0f;
        calibratedY = Math.round(calibratedY * 10) / 10.0f;
        float displayZ = Math.round(angleZ * 10) / 10.0f;

        boolean isHorizontal = Math.abs(calibratedX) <= HORIZONTAL_THRESHOLD &&
                Math.abs(calibratedY) <= HORIZONTAL_THRESHOLD;

        String status;
        if (isHorizontal) {
            status = getString(R.string.level_horizontal);
        } else if (Math.abs(calibratedX) > Math.abs(calibratedY)) {
            status = calibratedX > 0 ? getString(R.string.level_tilt_x) : getString(R.string.level_tilt_x_left);
        } else {
            status = calibratedY > 0 ? getString(R.string.level_tilt_y) : getString(R.string.level_tilt_y_down);
        }

        MeasurementHistory measurement = new MeasurementHistory(calibratedX, calibratedY, displayZ, status);
        historyList.add(0, measurement);
        saveHistory();
        android.widget.Toast.makeText(this, R.string.measurement_saved, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void loadHistory() {
        SharedPreferences prefs = getSharedPreferences("level_history", MODE_PRIVATE);
        String json = prefs.getString("history", "");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<MeasurementHistory>>() {}.getType();

        try {
            historyList = gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing history JSON", e);
            historyList = new ArrayList<>();
        }

        if (historyList == null) {
            historyList = new ArrayList<>();
        }
    }

    private void saveHistory() {
        SharedPreferences prefs = getSharedPreferences("level_history", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(historyList);
        prefs.edit().putString("history", json).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}