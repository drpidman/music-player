package com.drkryz.musicplayer.screens;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.drkryz.musicplayer.R;

public class PermissionActivity extends AppCompatActivity {

    Button allowExternal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        allowExternal = findViewById(R.id.allowExternal);

        allowExternal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("negated", "true");
                } else {
                    Log.e("conceded", "true");
                }
                ActivityCompat.requestPermissions(
                        PermissionActivity.this,
                        new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
                        140
                );
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 140) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent mainWindow = new Intent(this, MainActivity.class);
                startActivity(mainWindow);
                finish();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}