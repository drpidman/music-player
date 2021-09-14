package com.drkryz.musicplayer.screens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.managers.PermissionManager;

/*
 * configure app permissions in first initialization
 */

public class SplashActivity extends AppCompatActivity {

    private static int SPLASH_TIME = 3000;
    private static boolean PERMISSION_STATE;
    private Intent errorActivity;

    private PermissionManager permissionManager;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        permissionManager = new PermissionManager();
        permissionManager.RequestPermission(this);

        if (permissionManager.PermissionStatus(this)) {
            Intent mainActivity = new Intent(SplashActivity.this, MainActivity.class);
            loadMain(mainActivity, this);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 240) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent mainWindowActivity = new Intent(SplashActivity.this, MainActivity.class);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadMain(mainWindowActivity, SplashActivity.this);
                        finish();
                    }
                }, SPLASH_TIME);

            } else {
                errorActivity = new Intent(SplashActivity.this, PermissionDeniedActivity.class);
                startActivity(errorActivity);
                finish();
            }
        }
    }

    private void loadMain(Intent main, Activity ctx) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(main);
                finish();
            }
        }, SPLASH_TIME);
    }
}