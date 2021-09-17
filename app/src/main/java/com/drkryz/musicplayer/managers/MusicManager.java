package com.drkryz.musicplayer.managers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.listeners.media.MusicListeners;
import com.drkryz.musicplayer.screens.MainActivity;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;
import com.drkryz.musicplayer.utils.StorageUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadLocalRandom;


public class MusicManager {

    private MediaPlayer mediaPlayer;
    private final GlobalVariables globalVariables;
    private final BroadcastSenders broadcastSenders;
    private final Context ctx;

    public MusicManager(Context context) {
        this.ctx = context;
        globalVariables = (GlobalVariables) ctx.getApplicationContext();
        broadcastSenders = new BroadcastSenders(globalVariables.getContext());

    }

    private void initMediaPlayer() {
        Log.d("initMediaPlayer()", "init");
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnInfoListener(new MusicListeners(ctx));
        mediaPlayer.setOnErrorListener(new MusicListeners(ctx));
        mediaPlayer.setOnPreparedListener(new MusicListeners(ctx));
        mediaPlayer.setOnCompletionListener(new MusicListeners(ctx));
        mediaPlayer.setOnSeekCompleteListener(new MusicListeners(ctx));
        mediaPlayer.setOnBufferingUpdateListener(new MusicListeners(ctx));


        mediaPlayer.reset();

        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        );
        mediaPlayer.setWakeMode(ctx, PowerManager.PARTIAL_WAKE_LOCK);

        Log.e("activate:audiossid", "" + mediaPlayer.getAudioSessionId());

        try {
            mediaPlayer.setDataSource(globalVariables.activeAudio.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            globalVariables.musicService.stopSelf();
        }


        mediaPlayer.prepareAsync();

    }

    private void Play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            globalVariables.musicService.startForeground(0, null);
        }
    }

    private void Pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            globalVariables.resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void Stop() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            globalVariables.musicService.stopForeground(true);
            mediaPlayer.stop();
        }
    }

    private void Skip() {
        if (globalVariables.audioIndex == globalVariables.songList.size() -1) {
            globalVariables.audioIndex = 0;
            globalVariables.activeAudio = globalVariables.songList.get(globalVariables.audioIndex);
        } else {
            globalVariables.activeAudio = globalVariables.songList.get(++globalVariables.audioIndex);
            Log.d("new playing", "" + globalVariables.activeAudio.getTitle());
        }

        new StorageUtil(globalVariables.getContext()).storeAudioIndex(globalVariables.audioIndex);
        Stop();
        mediaPlayer.reset();
        initMediaPlayer();
        new BroadcastSenders(ctx).playbackNotification(BroadcastConstants.UpdateMetaData, GlobalVariables.Status.PLAYING);
    }

    private void Previous() {
        if (globalVariables.audioIndex == 0) {
            globalVariables.audioIndex = globalVariables.songList.size() -1;
            globalVariables.activeAudio = globalVariables.songList.get(globalVariables.audioIndex);
        } else {
            globalVariables.activeAudio = globalVariables.songList.get(--globalVariables.audioIndex);
        }

        new StorageUtil(globalVariables.getContext()).storeAudioIndex(globalVariables.audioIndex);
        mediaPlayer.reset();
        initMediaPlayer();
        new BroadcastSenders(ctx).playbackNotification(BroadcastConstants.UpdateMetaData, GlobalVariables.Status.PLAYING);
    }

    private void Resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(globalVariables.resumePosition);
            mediaPlayer.start();
        }
    }

    private void Reset() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }
    }

    private void Destroy() {
        Log.d("Destroy()", "called");
        if (mediaPlayer != null) {
            Stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    private final BroadcastReceiver initAction = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            initMediaPlayer();
        }
    };

    private final BroadcastReceiver playAction = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            Play();
        }
    };

    private final BroadcastReceiver pauseAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Pause();
        }
    };

    private final BroadcastReceiver stopAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Stop();
        }
    };


    private final BroadcastReceiver skipAction = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            Skip();
        }
    };

    private final BroadcastReceiver prevAction = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            Previous();
        }
    };

    private final BroadcastReceiver resumeAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Resume();
        }
    };

    private final BroadcastReceiver ResetAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Reset();
        }
    };

    private final BroadcastReceiver DestroyAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Destroy();
        }
    };


    private void RegisterInit() {
        ctx.registerReceiver(initAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestInit)
                );
    }

    private void RegisterPlay() {
        ctx.registerReceiver(playAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestPlay)
                );
    }
    private void RegisterPause() {
        ctx.registerReceiver(pauseAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestPause)
        );
    }
    private void RegisterStop() {
        ctx.registerReceiver(stopAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestStop)
        );
    }
    private void RegisterSkip() {
        ctx.registerReceiver(skipAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestSkip)
        );
    }
    private void RegisterPrev() {
        ctx.registerReceiver(prevAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestPrev)
        );
    }
    private void RegisterResume() {
        ctx.registerReceiver(resumeAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestResume)
        );
    }

    private void RegisterReset() {
        ctx.registerReceiver(ResetAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestReset)
                );
    }

    private void RegisterDestroy() {
        ctx.registerReceiver(DestroyAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestDestroy));
    }

    public void unregisterAll() {
        Log.d("unregisterAll", "unregistered receivers");
        ctx.unregisterReceiver(initAction);
        ctx.unregisterReceiver(playAction);
        ctx.unregisterReceiver(pauseAction);
        ctx.unregisterReceiver(stopAction);
        ctx.unregisterReceiver(skipAction);
        ctx.unregisterReceiver(prevAction);
        ctx.unregisterReceiver(resumeAction);
        ctx.unregisterReceiver(ResetAction);
        ctx.unregisterReceiver(DestroyAction);
    }

    public void registerAll() {
        Log.d("registerAll", "all listeners registered");
        RegisterInit();
        RegisterPlay();
        RegisterPause();
        RegisterSkip();
        RegisterPrev();
        RegisterStop();
        RegisterResume();
        RegisterReset();
        RegisterDestroy();
    }
}
