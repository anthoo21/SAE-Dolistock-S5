package com.example.saedolistocks5.popup;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.saedolistocks5.R;

public class CustomPopupSuppression extends AlertDialog {

    protected CustomPopupSuppression(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the dialog content view
        setContentView(R.layout.popup_validation_supp);

        // Get the buttons and set their click listeners
        Button menuButton = findViewById(R.id.homeButton);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(); // Dismiss the dialog
            }
        });
    }
    public static CustomPopupSuppression createDialog(Context context) {
        CustomPopupSuppression dialog = new CustomPopupSuppression(context);
        return dialog;
    }
}
