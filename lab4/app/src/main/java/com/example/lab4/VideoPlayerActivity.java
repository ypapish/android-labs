package com.example.lab4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class VideoPlayerActivity extends AppCompatActivity {

    private Button buttonSelectVideo, buttonPlay, buttonPause, buttonStop;
    private TextView textViewFileName;
    private VideoView videoView;
    private SeekBar seekBar;
    private Uri videoUri;
    private String fileName = "";
    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private MediaController mediaController;

    private final ActivityResultLauncher<Intent> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    videoUri = result.getData().getData();
                    if (videoUri != null) {
                        fileName = getFileName(videoUri);
                        textViewFileName.setText(fileName);
                        initializeVideoPlayer();
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openFilePicker();
                } else {
                    Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_video_player);

        initViews();
        setupListeners();
    }

    private void initViews() {
        buttonSelectVideo = findViewById(R.id.buttonSelectVideo);
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonPause = findViewById(R.id.buttonPause);
        buttonStop = findViewById(R.id.buttonStop);
        textViewFileName = findViewById(R.id.textViewFileName);
        videoView = findViewById(R.id.videoView);
        seekBar = findViewById(R.id.seekBar);

        mediaController = new MediaController(this);
    }

    private void setupListeners() {
        buttonSelectVideo.setOnClickListener(v -> checkPermissionAndPickFile());

        buttonPlay.setOnClickListener(v -> {
            if (videoView != null && !videoView.isPlaying()) {
                videoView.start();
                startUpdatingSeekBar();
            }
        });

        buttonPause.setOnClickListener(v -> {
            if (videoView != null && videoView.isPlaying()) {
                videoView.pause();
                handler.removeCallbacks(updateSeekBar);
            }
        });

        buttonStop.setOnClickListener(v -> {
            if (videoView != null) {
                videoView.stopPlayback();
                initializeVideoPlayer();
                seekBar.setProgress(0);
                handler.removeCallbacks(updateSeekBar);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && videoView != null) {
                    videoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void checkPermissionAndPickFile() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            openFilePicker();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            openFilePicker();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        videoPickerLauncher.launch(intent);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void initializeVideoPlayer() {
        if (videoUri != null) {
            videoView.setVideoURI(videoUri);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);

            videoView.setOnPreparedListener(mp -> {
                seekBar.setMax(videoView.getDuration());
                mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
                    mediaController.setAnchorView(videoView);
                });
            });

            videoView.setOnCompletionListener(mp -> {
                handler.removeCallbacks(updateSeekBar);
                seekBar.setProgress(0);
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, R.string.error_playback, Toast.LENGTH_SHORT).show();
                return false;
            });
        }
    }

    private void startUpdatingSeekBar() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (videoView != null && videoView.isPlaying()) {
                    int currentPosition = videoView.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
        handler.removeCallbacks(updateSeekBar);
    }
}