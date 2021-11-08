package com.drkryz.musicplayer.Screens;

import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.drkryz.musicplayer.Class.Default.UserFavorites;
import com.drkryz.musicplayer.Class.Default.UserPlaylist;
import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.Utils.PreferencesUtil;

import java.util.ArrayList;

/*
 * configure app permissions in first initialization
 */

public class SplashActivity extends Activity {

    private ArrayList<UserFavorites> favorites = new ArrayList<>();

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


    private void loadMain() {
        Intent intent = new Intent(SplashActivity.this, MusicActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadPermission() {
        Intent intent = new Intent(SplashActivity.this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }

}