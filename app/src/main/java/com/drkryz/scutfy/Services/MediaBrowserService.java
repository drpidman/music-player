package com.drkryz.scutfy.Services;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.drkryz.scutfy.R;

import java.util.List;

public class MediaBrowserService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mSession;


    @Override
    public void onCreate() {
        super.onCreate();

        mSession = new MediaSessionCompat(
                this, MediaBrowserService.class.getSimpleName()
        );

        setSessionToken(mSession.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(
                getString(R.string.app_name),
                null
        );
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }
}
