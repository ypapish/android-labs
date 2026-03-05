package com.example.lab2;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements
        InputFragment.OnOkButtonClickListener,
        ResultFragment.OnCancelButtonClickListener {

    private InputFragment inputFragment;
    private ResultFragment resultFragment;

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
    }

    @Override
    public void onOkButtonClicked(String password) {
        resultFragment = new ResultFragment();
        Bundle bundle = new Bundle();
        bundle.putString("password", password);
        resultFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container_result, resultFragment);
        transaction.commit();
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