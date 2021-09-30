package com.drkryz.musicplayer.utils;

import android.app.Application;
import android.content.Context;
import android.media.session.MediaController.TransportControls;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.support.v4.media.session.PlaybackStateCompat;

import com.drkryz.musicplayer.services.MusicService;

import java.util.ArrayList;

public class GlobalsUtil extends Application {

    private boolean serviceBound = false;
    // typed
    private ArrayList<SongUtil> musicList = new ArrayList<>();


    // global instances
    public MediaSession mediaSession;
    public MediaSessionManager mediaSessionManager;
    public TransportControls transportControls;
    public MusicService musicService;


    // media usage
    public int audioIndex = -1;
    public int resumePosition;
    public SongUtil activeAudio;
    public ArrayList<SongUtil> songList = musicList;

    public PlaybackStateCompat.Builder playbackStateCompat;

    public boolean isServiceBound() {
        return serviceBound;
    }

    public void setServiceBound(boolean serviceBound) {
        this.serviceBound = serviceBound;
    }

    public ArrayList<SongUtil> getMusicList() {
        return musicList;
    }

    public void setMusicList(ArrayList<SongUtil> musicList) {
        this.musicList = musicList;
    }

    public Context getContext() {
        return getApplicationContext();
    }

    public enum Status {
        PLAYING,
        PAUSED
    }
}