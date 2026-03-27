package com.example.lab4;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button buttonAudio = findViewById(R.id.buttonAudio);
        Button buttonVideo = findViewById(R.id.buttonVideo);
        Button buttonInternet = findViewById(R.id.buttonInternet);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonAudio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AudioPlayerActivity.class);
            startActivity(intent);
        });

        buttonVideo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
            startActivity(intent);
        });

        buttonInternet.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InternetMediaActivity.class);
            startActivity(intent);
        });
    }
}