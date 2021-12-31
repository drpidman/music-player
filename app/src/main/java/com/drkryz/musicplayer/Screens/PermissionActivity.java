package com.drkryz.musicplayer.Screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.drkryz.musicplayer.R;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

public class PermissionActivity extends AppCompatActivity {

    private SwitchCompat storagePermissionSwitcher, discordOAuthSwitcher;
    private ImageButton acceptButton;

    private AuthorizationRequest authRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);


        storagePermissionSwitcher = (SwitchCompat) findViewById(R.id.StorageEnableSwitcher);
        discordOAuthSwitcher = (SwitchCompat) findViewById(R.id.DiscordPermission);

        acceptButton = (ImageButton) findViewById(R.id.acceptButton);
        SwitchCompat sendUserCounter = findViewById(R.id.UserCountPermission);

        storagePermissionSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
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

                StoreStorageSwitcher(isChecked);
            }
        });

        sendUserCounter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                StoreUserCounterSwitcher(isChecked);
            }
        });


        discordOAuthSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
                            Uri.parse("https://api.drkryz.xyz/api/auth/discord"),
                            Uri.parse("https://discord.com/api/oauth2/token")
                    );

                    String clientId = "726932863722979388";

                    AuthorizationRequest.Builder authRequestBuilder =
                            new AuthorizationRequest.Builder(
                                    config,
                                    clientId,
                                    ResponseTypeValues.CODE,
                                    Uri.parse("https://api.drkryz.xyz/api/auth/discord/callback")
                            );

                    authRequest =
                            authRequestBuilder.setScope("identify")
                                    .setCodeVerifier(null)
                            .build();

                    doAuthorization();
                }
            }
        });


        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), SplashActivity.class));
                finish();
            }
        });
    }

    private void doAuthorization() {
        AuthorizationService service = new AuthorizationService(this);
        Intent authIntent = service.getAuthorizationRequestIntent(authRequest);
        Log.e(getPackageName(), "agaraga=" + authIntent);
        startActivityForResult(authIntent, 200);
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
                storagePermissionSwitcher.setEnabled(false);
                acceptButton.setVisibility(View.VISIBLE);
            } else {
                storagePermissionSwitcher.setChecked(false);
                StoreStorageSwitcher(false);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200) {
            if (data != null) {
                Log.e(getPackageName(), "" + data.getData().getQueryParameter("accessToken"));
            }
        }
    }
}