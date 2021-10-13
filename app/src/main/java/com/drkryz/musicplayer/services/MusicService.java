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
        Log.e("onBind()", "bind()");
        return iBinder;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(getPackageName(), "Removing task");

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
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
            audioIndex = globalsUtil.audioIndex = new PreferencesUtil(getBaseContext()).loadAudioIndex();

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
        if (musicManager != null) {

            PreferencesUtil preferencesUtil = new PreferencesUtil(getBaseContext());

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    preferencesUtil.StorePlayingState(globalsUtil.musicService.getPlayingState());
                    preferencesUtil.SetLastIndex(globalsUtil.audioIndex);
                }
            });

            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();


            return musicManager.getCurrentPosition();
        }
        return 0;
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