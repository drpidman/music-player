package com.drkryz.scutfy.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.drkryz.scutfy.R;

public class WelcomeActivity extends AppCompatActivity {

    View v;

    Button handlePermission, nextWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        handlePermission = findViewById(R.id.handlePermission);
        nextWindow = findViewById(R.id.nextButton);


        v = findViewById(R.id.view);

        Window window = getWindow();

        int colorTo = ((ColorDrawable) v.getBackground()).getColor();

        ObjectAnimator.ofObject(window, "statusBarColor", new ArgbEvaluator(), Color.TRANSPARENT, colorTo)
                .setDuration(100)
                .start();

        ObjectAnimator.ofObject(window, "navigationBarColor", new ArgbEvaluator(), Color.TRANSPARENT, Color.WHITE)
                .setDuration(100)
                .start();




        handlePermission.setOnClickListener((View v) -> {
            this.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            }, 140);
        });



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 140) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                nextWindow.setEnabled(true);
                handlePermission.setEnabled(false);

                nextWindow.setOnClickListener((View v) -> {
                    startActivity(new Intent(getBaseContext(), SplashActivity.class));
                    finish();
                });

            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getBaseContext().getPackageName(), null)));
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}