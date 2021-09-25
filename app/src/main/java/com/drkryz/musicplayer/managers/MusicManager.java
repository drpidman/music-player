package com.drkryz.musicplayer.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.drkryz.musicplayer.listeners.media.MusicListeners;
import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;
import com.drkryz.musicplayer.utils.StorageUtil;

import java.io.IOException;


public class MusicManager {

    private MediaPlayer mediaPlayer;
    private final GlobalVariables globalVariables;
    private final BroadcastSenders broadcastSenders;
    private AudioManager audioManager;

    private final Context ctx;

    BassBoost bassBoost;
    Equalizer equalizer;
    LoudnessEnhancer loudnessEnhancer;




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

        bassBoost = new BassBoost(0, mediaPlayer.getAudioSessionId());
        bassBoost.setEnabled(false);

        equalizer = new Equalizer(0, mediaPlayer.getAudioSessionId());
        equalizer.setEnabled(false);

        loudnessEnhancer = new LoudnessEnhancer(mediaPlayer.getAudioSessionId());
        equalizer.setEnabled(false);

    }


    private void Play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();

            updateCurrentPosition(PlaybackState.STATE_PLAYING);
            new StorageUtil(ctx).storePlayingState(mediaPlayer.isPlaying());
            Log.e("PlaybackState:::play", "" + new StorageUtil(globalVariables.getContext()).loadPlayingState());
        }
    }

    private void Pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();


            updateCurrentPosition(PlaybackState.STATE_PAUSED);
            new StorageUtil(ctx).storePlayingState(mediaPlayer.isPlaying());
            globalVariables.resumePosition = mediaPlayer.getCurrentPosition();
            Log.e("PlaybackState:::pause", "" + new StorageUtil(globalVariables.getContext()).loadPlayingState());
        }
    }

    private void Stop() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();


            new StorageUtil(ctx).storePlayingState(mediaPlayer.isPlaying());
            Log.e("PlaybackState:::stop", "" + new StorageUtil(globalVariables.getContext()).loadPlayingState());
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

        new StorageUtil(ctx).storePlayingState(mediaPlayer.isPlaying());
        new BroadcastSenders(ctx).playbackNotification(BroadcastConstants.UpdateMetaData, GlobalVariables.Status.PLAYING);
        Log.e("PlaybackState:::skip", "" + new StorageUtil(globalVariables.getContext()).loadPlayingState());
    }

    private void Previous() {
        if (globalVariables.audioIndex == 0) {
            globalVariables.audioIndex = globalVariables.songList.size() -1;
            globalVariables.activeAudio = globalVariables.songList.get(globalVariables.audioIndex);
        } else {
            globalVariables.activeAudio = globalVariables.songList.get(--globalVariables.audioIndex);
        }

        new StorageUtil(globalVariables.getContext()).storeAudioIndex(globalVariables.audioIndex);
        new StorageUtil(ctx).storePlayingState(mediaPlayer.isPlaying());
        Log.e("PlaybackState:::prev", "" + new StorageUtil(globalVariables.getContext()).loadPlayingState());

        mediaPlayer.reset();
        initMediaPlayer();
        new BroadcastSenders(ctx).playbackNotification(BroadcastConstants.UpdateMetaData, GlobalVariables.Status.PLAYING);
    }

    private void SeekTo(int seek) {
        if (mediaPlayer == null) return;
        mediaPlayer.seekTo(seek);
        updateCurrentPosition(PlaybackState.STATE_PLAYING);
    }

    private void Resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(globalVariables.resumePosition);
            mediaPlayer.start();

            bassBoost.setEnabled(false);
            equalizer.setEnabled(false);
            equalizer.setEnabled(false);

            updateCurrentPosition(PlaybackState.STATE_PLAYING);

            new StorageUtil(ctx).storePlayingState(mediaPlayer.isPlaying());
            Log.e("PlaybackState:::resume", "" + new StorageUtil(globalVariables.getContext()).loadPlayingState());
        }
    }

    private void Reset() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
            new StorageUtil(ctx).storePlayingState(mediaPlayer.isPlaying());
        }
    }

    private void Destroy() {
        Log.d("Destroy()", "called");
        if (mediaPlayer != null) {
            Stop();
            mediaPlayer.release();

            mediaPlayer = null;
            unregisterAll();
        }
    }


    private void updateCurrentPosition(int state) {
        Log.e("called", "media update called");
        if (mediaPlayer == null) return;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("called", "media update called");
                int currentPosition = mediaPlayer.getCurrentPosition();
                PlaybackState playbackState =
                        new PlaybackState.Builder()
                                .setState(state, currentPosition, 1)
                                .setActions(PlaybackState.ACTION_SEEK_TO)
                                .build();

                globalVariables.mediaSession.setPlaybackState(playbackState);

                new StorageUtil(ctx).storeTotalDuration(mediaPlayer.getDuration());
            }
        }, 100);
    }


    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getTotalDuration() {
        return mediaPlayer.getDuration();
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
        @Override
        public void onReceive(Context context, Intent intent) {
            Skip();
        }
    };

    private final BroadcastReceiver prevAction = new BroadcastReceiver() {
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

    private final BroadcastReceiver seekAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long seek = intent.getLongExtra("seekTo", 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SeekTo(Math.toIntExact(seek));
            } else {
                String seekString = String.valueOf(seek);
                SeekTo(Integer.parseInt(seekString));
            }
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

    private void RegisterSeek() {
        ctx.registerReceiver(seekAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RequestSeek)
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
        ctx.unregisterReceiver(seekAction);
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
        RegisterSeek();
        RegisterReset();
        RegisterDestroy();
    }
}
