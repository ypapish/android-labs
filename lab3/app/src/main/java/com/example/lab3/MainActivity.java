package com.example.lab3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements
        InputFragment.OnOkButtonClickListener,
        ResultFragment.OnCancelButtonClickListener {

    private InputFragment inputFragment;
    private ResultFragment resultFragment;
    private static final String FILE_NAME = "password.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        inputFragment = (InputFragment) fragmentManager.findFragmentById(R.id.fragment_container_input);

        Button buttonOpen = findViewById(R.id.buttonOpenStorage);
        buttonOpen.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onOkButtonClicked(String password) {
        savePasswordToFile(password);

        resultFragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putString("password", password);
        resultFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_result, resultFragment);
        transaction.commit();
    }

    private void savePasswordToFile(String password) {
        try (FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE)) {
            fos.write(password.getBytes());
            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            android.util.Log.e("MainActivity", "Error saving password", e);
        }
    }

    @Override
    public void onCancelButtonClicked() {
        if (resultFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(resultFragment);
            transaction.commit();
            resultFragment = null;
        }

        if (inputFragment != null) {
            inputFragment.clearInputField();
        }
    }
}