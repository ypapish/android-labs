package com.example.lab3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class DataActivity extends AppCompatActivity {

    private TextView textViewData;
    private static final String FILE_NAME = "password.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        textViewData = findViewById(R.id.textViewData);
        Button buttonClear = findViewById(R.id.buttonClear);

        loadDataFromFile();

        buttonClear.setOnClickListener(v -> {
            clearFile();
            loadDataFromFile();
            Toast.makeText(DataActivity.this, R.string.data_cleared, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDataFromFile() {
        File file = new File(getFilesDir(), FILE_NAME);

        if (file.exists()) {
            try (FileInputStream fis = openFileInput(FILE_NAME);
                 InputStreamReader isr = new InputStreamReader(fis);
                 BufferedReader br = new BufferedReader(isr)) {


                StringBuilder content = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line);
                }

                String password = content.toString().trim();
                if (password.isEmpty()) {
                    textViewData.setText(R.string.no_data);
                } else {
                    textViewData.setText(password);
                }

            } catch (IOException e) {
                textViewData.setText(R.string.error_reading_file);
                android.util.Log.e("DataActivity", "Error reading file", e);
            }
        } else {
            textViewData.setText(R.string.no_data);
        }
    }

    private void clearFile() {
        File file = new File(getFilesDir(), FILE_NAME);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                Toast.makeText(this, "Помилка при видаленні файлу", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDataFromFile();
    }
}