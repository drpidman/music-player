package com.drkryz.musicplayer.listeners.media;

import android.content.Context;
import android.media.session.MediaSession;
import android.os.Build;

import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.managers.NotificationBuilderManager;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.ApplicationUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

public class MediaSessionCallbacks extends MediaSession.Callback {

    private final ApplicationUtil applicationUtil;
    private final BroadcastUtils broadcastUtils;
    private final PreferencesUtil preferencesUtil;
    private final NotificationBuilderManager notificationBuilderManager;


    public MediaSessionCallbacks(Context context) {
        applicationUtil = (ApplicationUtil) context.getApplicationContext();
        broadcastUtils = new BroadcastUtils(context);
        preferencesUtil = new PreferencesUtil(context);
        notificationBuilderManager = new NotificationBuilderManager(context);
    }

    @Override
    public void onPlay() {
        super.onPlay();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);

    }
}