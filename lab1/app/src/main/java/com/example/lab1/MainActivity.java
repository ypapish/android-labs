package com.example.lab1;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private EditText editTextPassword;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editTextPassword = findViewById(R.id.editTextPassword);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        Button buttonOk = findViewById(R.id.buttonOk);
        textViewResult = findViewById(R.id.textViewResult);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean hadFocus = editTextPassword.hasFocus();
            String currentText = editTextPassword.getText().toString();
            int selectionStart = editTextPassword.getSelectionStart();
            int selectionEnd = editTextPassword.getSelectionEnd();

            if (checkedId == R.id.radioShowStars) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else if (checkedId == R.id.radioShowText) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }


            editTextPassword.setTypeface(android.graphics.Typeface.DEFAULT);


            editTextPassword.setText(currentText);
            editTextPassword.setSelection(selectionStart, selectionEnd);


            if (hadFocus) {
                editTextPassword.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextPassword, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        buttonOk.setOnClickListener(v -> checkAndShowPassword());
    }

    private void checkAndShowPassword() {
        String password = editTextPassword.getText().toString().trim();

        if (password.isEmpty()) {
            showAlertDialog(
                    getString(R.string.error_title),
                    getString(R.string.error_message)
            );
        } else {
            textViewResult.setText(
                    getString(R.string.result_text, password)
            );
        }
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok_button, null)
                .show();
    }
}