package com.drkryz.musicplayer.services;

import static com.drkryz.musicplayer.utils.BroadcastConstants.ACTION_PAUSE;
import static com.drkryz.musicplayer.utils.BroadcastConstants.ACTION_PLAY;
import static com.drkryz.musicplayer.utils.BroadcastConstants.ACTION_PREV;
import static com.drkryz.musicplayer.utils.BroadcastConstants.ACTION_SKIP;
import static com.drkryz.musicplayer.utils.BroadcastConstants.ACTION_STOP;
import static com.drkryz.musicplayer.utils.BroadcastConstants.Resume;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.service.media.MediaBrowserService;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.managers.MusicManager;
import com.drkryz.musicplayer.managers.NotificationBuilderManager;
import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;
import com.drkryz.musicplayer.utils.Song;
import com.drkryz.musicplayer.utils.StorageUtil;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {

    private final IBinder iBinder = new LocalBinder();
    private GlobalVariables globalVariables;
    private BroadcastSenders broadcastSenders;
    private MusicManager musicManager;
    private NotificationBuilderManager notificationBuilderManager;


    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("onUnbind()", intent.toString());
        globalVariables.mediaSession.release();
        broadcastSenders.playbackManager(BroadcastConstants.RequestDestroy, 0);
        notificationBuilderManager.removeNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("onDestroy()", "called");

        broadcastSenders.playbackManager(BroadcastConstants.RequestDestroy, 0);

        musicManager.unregisterAll();
        notificationBuilderManager.unregisterAll();

        unregisterReceiver(nowPlaying);
        unregisterReceiver(pausePlaying);
        unregisterReceiver(resumePlaying);
        unregisterReceiver(skipPlaying);
        unregisterReceiver(previousPlaying);

        new StorageUtil(this).clearCachedAudioPlaylist();
        new StorageUtil(this).clearCachedPlayingStatus();

        super.onDestroy();
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        globalVariables = (GlobalVariables) getApplicationContext();
        broadcastSenders = (BroadcastSenders) new BroadcastSenders(getApplicationContext());


        Log.d("started", "" + startId);

        try {

            StorageUtil storage = new StorageUtil(getApplicationContext());
            globalVariables.songList = storage.loadAudio();
            globalVariables.audioIndex = storage.loadAudioIndex();

            if (globalVariables.audioIndex != -1 && globalVariables.audioIndex < globalVariables.songList.size()) {
                globalVariables.activeAudio = globalVariables.songList.get(globalVariables.audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }


        if (globalVariables.mediaSessionManager == null) {
            try {
                notificationBuilderManager.initMediaSession();
                broadcastSenders.playbackManager(BroadcastConstants.RequestInit, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }

            broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
            startForeground(0, null);
        }

        handleActions(intent);
        return START_NOT_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        musicManager = new MusicManager(getBaseContext());
        notificationBuilderManager = new NotificationBuilderManager(getBaseContext());


        registerPlay();
        registerPause();
        registerResume();
        registerSkip();
        registerPrevious();

        musicManager.registerAll();
        notificationBuilderManager.registerAll();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Toast.makeText(this, "HOW, pouca memoria :(", Toast.LENGTH_LONG).show();
        stopSelf();
    }


    private void NowPlaying() {
        int audioIndex = globalVariables.audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();

        if (audioIndex != -1 && audioIndex < globalVariables.songList.size()) {
            globalVariables.activeAudio = globalVariables.songList.get(audioIndex);
        } else {
            stopSelf();
        }


        broadcastSenders.playbackManager(BroadcastConstants.RequestStop, 0);
        broadcastSenders.playbackManager(BroadcastConstants.RequestReset, 0);
        broadcastSenders.playbackManager(BroadcastConstants.RequestInit, 0);

        broadcastSenders.playbackNotification(BroadcastConstants.UpdateMetaData, null);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }

    private void Pause() {
        broadcastSenders.playbackManager(BroadcastConstants.RequestPause, 0);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PAUSED);
    }

    private void Resume() {
        broadcastSenders.playbackManager(BroadcastConstants.RequestResume, 0);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }

    private void Skip() {
        broadcastSenders.playbackManager(BroadcastConstants.RequestSkip, 0);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }

    private void Previous() {
        broadcastSenders.playbackManager(BroadcastConstants.RequestPrev, 0);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }



    private final BroadcastReceiver nowPlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NowPlaying();
        }
    };

    private final BroadcastReceiver pausePlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Pause();
        }
    };

    private final BroadcastReceiver resumePlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Resume();
        }
    };

    private final BroadcastReceiver skipPlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Skip();
        }
    };

    private final BroadcastReceiver previousPlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Previous();
        }
    };




    private void registerPlay() {
        broadcastSenders = new BroadcastSenders(getApplicationContext());
        registerReceiver(nowPlaying, broadcastSenders.playbackUIFilter(BroadcastConstants.Play));
    }

    private void registerPause() {
        broadcastSenders = new BroadcastSenders(getApplicationContext());
        registerReceiver(pausePlaying, broadcastSenders.playbackUIFilter(BroadcastConstants.Pause));
    }


    private void registerResume() {
        broadcastSenders = new BroadcastSenders(getApplicationContext());
        registerReceiver(resumePlaying, broadcastSenders.playbackUIFilter(BroadcastConstants.Resume));
    }

    private void registerSkip() {
        broadcastSenders = new BroadcastSenders(getApplicationContext());
        registerReceiver(skipPlaying, broadcastSenders.playbackUIFilter(BroadcastConstants.Skip));
    }

    private void registerPrevious() {
        broadcastSenders = new BroadcastSenders(getApplicationContext());
        registerReceiver(previousPlaying, broadcastSenders.playbackUIFilter(BroadcastConstants.Prev));
    }




    private void handleActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            globalVariables.transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            globalVariables.transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_SKIP)) {
            globalVariables.transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREV)) {
            globalVariables.transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            globalVariables.transportControls.stop();
        }
    }


    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}