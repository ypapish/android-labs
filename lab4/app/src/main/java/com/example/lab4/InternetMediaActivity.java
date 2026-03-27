package com.example.lab4;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Locale;

public class InternetMediaActivity extends AppCompatActivity {

    private EditText editTextUrl;
    private Button buttonLoad;
    private Button buttonPlay;
    private Button buttonPause;
    private Button buttonStop;
    private RadioGroup radioGroupFileType;
    private FrameLayout frameLayoutContent;
    private TextView textViewFileName;
    private TextView textViewTime;
    private SeekBar seekBar;
    private View controlsLayout;

    private MediaPlayer mediaPlayer;
    private VideoView videoView;
    private boolean isAudioMode = true;
    private String currentUrl = "";
    private String currentFileName = "";
    private final Handler handler = new Handler();
    private Runnable updateSeekBar;

    private static final String TAG = "InternetMediaActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_internet_media);

        initViews();
        setupListeners();
    }

    private void initViews() {
        editTextUrl = findViewById(R.id.editTextUrl);
        buttonLoad = findViewById(R.id.buttonLoad);
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonPause = findViewById(R.id.buttonPause);
        buttonStop = findViewById(R.id.buttonStop);
        radioGroupFileType = findViewById(R.id.radioGroupFileType);
        frameLayoutContent = findViewById(R.id.frameLayoutContent);
        textViewTime = findViewById(R.id.textViewTime);
        seekBar = findViewById(R.id.seekBar);
        controlsLayout = findViewById(R.id.controlsLayout);

        textViewFileName = findViewById(R.id.textViewFileName);
        if (textViewFileName == null) {
            textViewFileName = new TextView(this);
        }
    }

    private void setupListeners() {
        radioGroupFileType.setOnCheckedChangeListener((group, checkedId) -> {
            isAudioMode = (checkedId == R.id.radioAudio);
            clearContent();
        });

        buttonLoad.setOnClickListener(v -> {
            String url = editTextUrl.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(this, R.string.enter_url, Toast.LENGTH_SHORT).show();
                return;
            }
            currentUrl = url;
            currentFileName = getFileNameFromUrl(url);
            updateFileNameDisplay();
            loadMedia();
        });

        buttonPlay.setOnClickListener(v -> {
            if (isAudioMode) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    startUpdatingSeekBar();
                }
            } else {
                if (videoView != null && !videoView.isPlaying()) {
                    videoView.start();
                    startUpdatingSeekBar();
                }
            }
        });

        buttonPause.setOnClickListener(v -> {
            if (isAudioMode) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    handler.removeCallbacks(updateSeekBar);
                }
            } else {
                if (videoView != null && videoView.isPlaying()) {
                    videoView.pause();
                    handler.removeCallbacks(updateSeekBar);
                }
            }
        });

        buttonStop.setOnClickListener(v -> {
            if (isAudioMode) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    try {
                        mediaPlayer.prepare();
                        mediaPlayer.seekTo(0);
                    } catch (IOException e) {
                        Log.e(TAG, "Error preparing media player", e);
                    }
                    seekBar.setProgress(0);
                    updateTimeDisplay(0, mediaPlayer.getDuration());
                    handler.removeCallbacks(updateSeekBar);
                }
            } else {
                if (videoView != null) {
                    videoView.stopPlayback();
                    loadVideo();
                    seekBar.setProgress(0);
                    updateTimeDisplay(0, videoView.getDuration());
                    handler.removeCallbacks(updateSeekBar);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (isAudioMode && mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                    } else if (!isAudioMode && videoView != null) {
                        videoView.seekTo(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void clearContent() {
        handler.removeCallbacks(updateSeekBar);

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (videoView != null) {
            videoView.stopPlayback();
            frameLayoutContent.removeView(videoView);
            videoView = null;
        }

        if (controlsLayout != null) {
            controlsLayout.setVisibility(View.GONE);
        }
        if (seekBar != null) {
            seekBar.setVisibility(View.GONE);
            seekBar.setProgress(0);
        }
        if (textViewTime != null) {
            textViewTime.setVisibility(View.GONE);
            textViewTime.setText(R.string.default_time);
        }

        currentFileName = "";
        updateFileNameDisplay();
    }

    private void updateFileNameDisplay() {
        if (textViewFileName != null) {
            if (currentFileName.isEmpty()) {
                textViewFileName.setText(R.string.no_file_selected);
            } else {
                textViewFileName.setText(currentFileName);
            }
        }
    }

    private String getFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < url.length() - 1) {
            String fileName = url.substring(lastSlash + 1);
            int queryIndex = fileName.indexOf('?');
            if (queryIndex != -1) {
                fileName = fileName.substring(0, queryIndex);
            }
            return fileName;
        }
        return "media_file";
    }

    private void loadMedia() {
        clearContent();

        if (isAudioMode) {
            loadAudio();
        } else {
            loadVideo();
        }
    }

    private void loadAudio() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(currentUrl);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mediaPlayer.getDuration();
                seekBar.setMax(duration);
                updateTimeDisplay(0, duration);
                controlsLayout.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);
                textViewTime.setVisibility(View.VISIBLE);
                Toast.makeText(this, R.string.media_loaded, Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                handler.removeCallbacks(updateSeekBar);
                seekBar.setProgress(0);
                updateTimeDisplay(0, mediaPlayer.getDuration());
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, R.string.error_playback, Toast.LENGTH_SHORT).show();
                return false;
            });

        } catch (IOException e) {
            Log.e(TAG, "Error loading audio", e);
            Toast.makeText(this, R.string.error_playback, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadVideo() {
        videoView = new VideoView(this);
        videoView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        frameLayoutContent.addView(videoView);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setVideoURI(Uri.parse(currentUrl));

        videoView.setOnPreparedListener(mp -> {
            int duration = videoView.getDuration();
            seekBar.setMax(duration);
            controlsLayout.setVisibility(View.VISIBLE);
            seekBar.setVisibility(View.VISIBLE);
            textViewTime.setVisibility(View.VISIBLE);
            updateTimeDisplay(0, duration);
            Toast.makeText(this, R.string.media_loaded, Toast.LENGTH_SHORT).show();
        });

        videoView.setOnCompletionListener(mp -> {
            handler.removeCallbacks(updateSeekBar);
            seekBar.setProgress(0);
            updateTimeDisplay(0, videoView.getDuration());
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, R.string.error_playback, Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private void startUpdatingSeekBar() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (isAudioMode && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    seekBar.setProgress(currentPosition);
                    updateTimeDisplay(currentPosition, duration);
                    handler.postDelayed(this, 1000);
                } else if (!isAudioMode && videoView != null && videoView.isPlaying()) {
                    int currentPosition = videoView.getCurrentPosition();
                    int duration = videoView.getDuration();
                    seekBar.setProgress(currentPosition);
                    updateTimeDisplay(currentPosition, duration);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBar);
    }

    private void updateTimeDisplay(int currentPosition, int duration) {
        if (textViewTime != null) {
            String timeText = formatTime(currentPosition) + " / " + formatTime(duration);
            textViewTime.setText(timeText);
        }
    }

    private String formatTime(int milliseconds) {
        if (milliseconds <= 0) {
            return "00:00";
        }
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (videoView != null) {
            videoView.stopPlayback();
        }
        handler.removeCallbacks(updateSeekBar);
    }
}