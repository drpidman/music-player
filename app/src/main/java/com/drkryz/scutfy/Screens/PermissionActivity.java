package com.drkryz.scutfy.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.drkryz.scutfy.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class PermissionActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageButton instagramProfileBtn, twitterProfileBtn,
    facebookProfileBtn, acceptPermission;
    private Button goPermission;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
            }
        });

        instagramProfileBtn = findViewById(R.id.link_Instagram_support);
        twitterProfileBtn = findViewById(R.id.link_Twitter_support);
        facebookProfileBtn = findViewById(R.id.link_Facebook_support);
        acceptPermission = findViewById(R.id.acceptPermission);
        goPermission = findViewById(R.id.goPermission);


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        instagramProfileBtn.setOnClickListener(this);
        twitterProfileBtn.setOnClickListener(this);
        facebookProfileBtn.setOnClickListener(this);
        acceptPermission.setOnClickListener(this);
        goPermission.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void StoreStorageSwitcher(boolean isChecked) {
        SharedPreferences preferences = getSharedPreferences("com.drkryz.musicplayer.permission.storageAccess.check", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("permission.storage.checked", isChecked);
        editor.apply();
    }

    private boolean LoadStorageSwitcher() {
        SharedPreferences preferences = getSharedPreferences("com.drkryz.musicplayer.permission.storageAccess.check", Context.MODE_PRIVATE);
        return preferences.getBoolean("permission.storage.checked", false);
    }

    private void StoreUserCounterSwitcher(boolean isChecked) {
        SharedPreferences preferences = getSharedPreferences("com.drkryz.musicplayer.permission.userCounter.check", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("permission.userCounter.checked", isChecked);
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 140) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                acceptPermission.setVisibility(View.VISIBLE);
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(
                        PermissionActivity.this, new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        }, 140
                );
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void goUri(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == instagramProfileBtn.getId()) {
            goUri("https://instagram.com/Scutfy");
        } else if (view.getId() == facebookProfileBtn.getId()) {
            goUri("https://facebook.com/Scutfy-102889618941068");
        } else if (view.getId() == twitterProfileBtn.getId()) {
            goUri("https://twitter.com/drkryzProject");
        } else if (view.getId() == acceptPermission.getId()) {
            startActivity(new Intent(getBaseContext(), SplashActivity.class));
            finish();
        } else if (view.getId() == goPermission.getId()) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                        PermissionActivity.this, new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        }, 140
                );
            }
        }
    }
}