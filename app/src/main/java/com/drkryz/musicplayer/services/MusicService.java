package com.drkryz.musicplayer.services;

import static com.drkryz.musicplayer.constants.BroadcastConstants.ACTION_PAUSE;
import static com.drkryz.musicplayer.constants.BroadcastConstants.ACTION_PLAY;
import static com.drkryz.musicplayer.constants.BroadcastConstants.ACTION_PREV;
import static com.drkryz.musicplayer.constants.BroadcastConstants.ACTION_SKIP;
import static com.drkryz.musicplayer.constants.BroadcastConstants.ACTION_STOP;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.drkryz.musicplayer.managers.MusicManager;
import com.drkryz.musicplayer.managers.NotificationBuilderManager;
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

public class MusicService extends Service {

    private final IBinder iBinder = new LocalBinder();
    private GlobalsUtil globalsUtil;
    private BroadcastUtils broadcastUtils;
    private NotificationBuilderManager notificationBuilderManager;
    private MusicManager musicManager;


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.e(getPackageName() + ":onUnbind():Service", "called");
            NotificationManagerCompat notificationManagerCompat =
                    NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.cancelAll();
        } else {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        }

        globalsUtil.mediaSession.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e(getPackageName() + "onTaskRemoved()", "called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy()", "called");
        unregisterReceiver(nowPlaying);

        musicManager.removeAudioFocus();

        broadcastUtils.playbackManager(BroadcastConstants.RequestDestroy, 0);
        notificationBuilderManager.unregisterAll();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicManager = new MusicManager(getBaseContext());
        notificationBuilderManager = new NotificationBuilderManager(getBaseContext());


        registerNowPlaying();

        musicManager.registerAll();
        notificationBuilderManager.registerAll();
    }



    @SuppressLint("ResourceAsColor")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        globalsUtil = (GlobalsUtil) getApplicationContext();
        broadcastUtils = (BroadcastUtils) new BroadcastUtils(getApplicationContext());


        Log.d("started", "" + startId);

        try {

            PreferencesUtil storage = new PreferencesUtil(getApplicationContext());
            globalsUtil.songList = storage.loadAudio();
            globalsUtil.audioIndex = storage.loadAudioIndex();

            if (globalsUtil.audioIndex != -1 && globalsUtil.audioIndex < globalsUtil.songList.size()) {
                globalsUtil.activeAudio = globalsUtil.songList.get(globalsUtil.audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        if (musicManager.requestAudioFocus() == false) {
            stopSelf();
        }

        if (globalsUtil.mediaSessionManager == null) {
            try {
                notificationBuilderManager.initMediaSession();
                broadcastUtils.playbackManager(BroadcastConstants.RequestInit, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }

            broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);
        }

        startForeground(0, null);

        handleActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Toast.makeText(this, "HOW, pouca memoria :(", Toast.LENGTH_LONG).show();
        stopSelf();
    }


    private void NowPlaying() {
        int audioIndex = globalsUtil.audioIndex = new PreferencesUtil(getApplicationContext()).loadAudioIndex();

        if (audioIndex != -1 && audioIndex < globalsUtil.songList.size()) {
            globalsUtil.activeAudio = globalsUtil.songList.get(audioIndex);
        } else {
            stopSelf();
        }


        broadcastUtils.playbackManager(BroadcastConstants.RequestStop, 0);
        broadcastUtils.playbackManager(BroadcastConstants.RequestReset, 0);
        broadcastUtils.playbackManager(BroadcastConstants.RequestInit, 0);

        broadcastUtils.playbackNotification(BroadcastConstants.UpdateMetaData, null);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);
    }

    private final BroadcastReceiver nowPlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NowPlaying();
        }
    };


    private void registerNowPlaying() {
        registerReceiver(nowPlaying, new BroadcastUtils(getBaseContext())
                .playbackUIFilter(BroadcastConstants.Play));
    }


    private void handleActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            globalsUtil.transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            globalsUtil.transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_SKIP)) {
            globalsUtil.transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREV)) {
            globalsUtil.transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            globalsUtil.transportControls.stop();
        }
    }


    public int getCurrentPosition() {
        return musicManager.getCurrentPosition();
    }

    public int getTotalDuration() {
        return musicManager.getTotalDuration();
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}