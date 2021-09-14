package com.drkryz.musicplayer.managers;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class PermissionManager {

    public void RequestPermission(Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE}, 240);
        }
    }

    public boolean PermissionStatus(Activity context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
}

