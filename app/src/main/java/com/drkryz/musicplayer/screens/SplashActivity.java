package com.drkryz.musicplayer.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import com.drkryz.musicplayer.R;

/*
 * configure app permissions in first initialization
 */

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME = 3000;
    private static boolean PERMISSION_STATE;
    private Intent errorActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            loadPermission();
        } else {
            loadMain();
        }
    }


    private void loadMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, PlayerActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME);
    }

    private void loadPermission() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, PermissionActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME);
    }
}