package com.drkryz.scutfy.Screens;

import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.drkryz.scutfy.R;

import java.util.ArrayList;

/*
 * configure app permissions in first initialization
 */

public class SplashActivity extends Activity {


    private final ArrayList<UserPlaylist> musics = new ArrayList<UserPlaylist>();


    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            loadPermission();
        } else {
            loadMain();
        }


        setContentView(R.layout.activity_splash);
    }


    @SuppressLint("Range")
    private void loadMain() {

        Intent intent = new Intent(SplashActivity.this, MusicActivity.class);

        startActivity(intent);
        finish();
    }

    private void loadPermission() {
        Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

}