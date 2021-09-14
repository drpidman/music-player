package com.drkryz.musicplayer.utils;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaController.TransportControls;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.drkryz.musicplayer.services.MusicService;

import java.util.ArrayList;

public class GlobalVariables extends Application {

    // bool
    private boolean isPlaying = false;
    private boolean serviceBound = false;

    // typed
    private ArrayList<Song> musicList = new ArrayList<>();


    // global instances
    public MediaSession mediaSession;
    public MediaSessionManager mediaSessionManager;
    public TransportControls transportControls;

    public MusicService musicService;


    // media usage
    public int audioIndex = -1;
    public int resumePosition;
    public Song activeAudio;
    public ArrayList<Song> songList = musicList;


    private int startId;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isServiceBound() {
        return serviceBound;
    }

    public void setServiceBound(boolean serviceBound) {
        this.serviceBound = serviceBound;
    }

    public ArrayList<Song> getMusicList() {
        return musicList;
    }

    public void setMusicList(ArrayList<Song> musicList) {
        this.musicList = musicList;
    }

    public Context getContext() {
        return getApplicationContext();
    }



    public int getStartId() {
        return startId;
    }

    public void setStartId(int startId) {
        this.startId = startId;
    }


    public enum Status {
        PLAYING,
        PAUSED
    }
}