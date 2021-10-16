package com.drkryz.musicplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.drkryz.musicplayer.constants.BroadcastConstants;

public class BroadcastUtils {

    private final ApplicationUtil applicationUtil;

    public BroadcastUtils(Context context) {
        applicationUtil = (ApplicationUtil) context.getApplicationContext();
    }

    public void playbackManager(String action, long seekVal) {
        Intent intent;

    }

    public void playbackUIManager(String action, boolean isPlaying) {

    }

    public IntentFilter playbackUIFilter(String action) {
        return null;
    }


    public IntentFilter playbackFilter(String action) {
        return null;
    }

    public void playbackNotification(String action, ApplicationUtil.Status status) {
        Intent intent;
    }
}