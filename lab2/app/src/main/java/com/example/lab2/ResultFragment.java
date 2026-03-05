package com.example.lab2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class ResultFragment extends Fragment {

    private TextView textViewResult;
    private OnCancelButtonClickListener callback;

    public interface OnCancelButtonClickListener {
        void onCancelButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCancelButtonClickListener) {
            callback = (OnCancelButtonClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCancelButtonClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        textViewResult = view.findViewById(R.id.textViewResult);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        if (getArguments() != null) {
            String password = getArguments().getString("password");
            textViewResult.setText(getString(R.string.result_text, password));
        }

        buttonCancel.setOnClickListener(v -> {
            if (callback != null) {
                callback.onCancelButtonClicked();
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}