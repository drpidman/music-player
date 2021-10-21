package com.drkryz.musicplayer.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.palette.graphics.Palette;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.drkryz.musicplayer.R;

/*
 * configure app permissions in first initialization
 */

public class SplashActivity extends Activity {

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


    private void loadMain() {
        Intent intent = new Intent(SplashActivity.this, PlayerActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadPermission() {
        Intent intent = new Intent(SplashActivity.this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }

}