package com.drkryz.musicplayer.services;

import static com.drkryz.musicplayer.constants.BroadcastConstants.ACTION_CLOSE;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

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
    private PreferencesUtil preferencesUtil;


    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("onBind()", "bind()");
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(getPackageName(), "onUnbind():Service");
        preferencesUtil.clearCover();
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.e(getPackageName(), "onRebind():Service");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(getPackageName(), "Removing task");

        musicManager.unregisterAll();
        notificationBuilderManager.unregisterAll();

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        broadcastUtils.playbackManager(BroadcastConstants.RequestDestroy, 0);

        musicManager.unregisterAll();
        notificationBuilderManager.unregisterAll();
        unregisterReceiver(nowPlaying);

        musicManager.removeAudioFocus();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicManager = new MusicManager(getBaseContext());
        notificationBuilderManager = new NotificationBuilderManager(getBaseContext());
        preferencesUtil = new PreferencesUtil(getBaseContext());


        registerNowPlaying();

        musicManager.registerAll();
        notificationBuilderManager.registerAll();

        HandlerThread handlerThread = new HandlerThread("MusicPlayer",
                Process.THREAD_PRIORITY_BACKGROUND
                );

        handlerThread.start();


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
                musicManager.initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }

            broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);
        }

        startForeground(0, null);

        handleActions(intent);
        return START_NOT_STICKY;
    }

    private void NowPlaying() {


        int audioIndex = 0;

        if (globalsUtil != null) {
            audioIndex = globalsUtil.audioIndex = preferencesUtil.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < globalsUtil.songList.size()) {
                globalsUtil.activeAudio = globalsUtil.songList.get(audioIndex);
            } else {
                stopSelf();
            }

            musicManager.Stop();
            musicManager.Reset();
            musicManager.initMediaPlayer();

            broadcastUtils.playbackNotification(BroadcastConstants.UpdateMetaData, null);
            broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);
        } else {

        }
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
        } else if (actionString.equalsIgnoreCase(ACTION_CLOSE)) {
            musicManager.Destroy();
        }
    }


    public int getCurrentPosition() {

        return musicManager.getCurrentPosition();
    }

    public int getTotalDuration() {
        if (musicManager != null) return musicManager.getTotalDuration();
        return 0;
    }

    public int getMediaSessionId() {
        return musicManager.getMediaSession();
    }

    public boolean getPlayingState() {
        return musicManager.getPlayingState();
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}