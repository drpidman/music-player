package com.drkryz.musicplayer.utils;

import android.app.Application;
import android.content.Context;
import android.media.session.MediaController.TransportControls;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.drkryz.musicplayer.services.MusicService;

import java.util.ArrayList;

public class ApplicationUtil extends Application {

    public enum Status {
        PLAYING,
        PAUSED
    }
}