package com.example.lab2;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class InputFragment extends Fragment {

    private EditText editTextPassword;
    private OnOkButtonClickListener callback;

    public interface OnOkButtonClickListener {
        void onOkButtonClicked(String password);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOkButtonClickListener) {
            callback = (OnOkButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnOkButtonClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input, container, false);

        editTextPassword = view.findViewById(R.id.editTextPassword);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        Button buttonOk = view.findViewById(R.id.buttonOk);

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
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editTextPassword, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        buttonOk.setOnClickListener(v -> checkAndSendPassword());

        return view;
    }

    private void checkAndSendPassword() {
        String password = editTextPassword.getText().toString().trim();

        if (password.isEmpty()) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.error_title)
                    .setMessage(R.string.error_message)
                    .setPositiveButton(R.string.ok_button, null)
                    .show();
        } else {
            callback.onOkButtonClicked(password);
        }
    }

    public void clearInputField() {
        if (editTextPassword != null) {
            editTextPassword.setText("");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}