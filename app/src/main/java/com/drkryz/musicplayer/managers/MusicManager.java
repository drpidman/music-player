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
import android.net.wifi.aware.PeerHandle;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.drkryz.musicplayer.listeners.media.MusicListeners;
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.screens.PlayerActivity;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

import java.io.IOException;


public class MusicManager
    implements AudioManager.OnAudioFocusChangeListener
{

    private MediaPlayer mediaPlayer;
    private final GlobalsUtil globalsUtil;
    private final BroadcastUtils broadcastUtils;
    private final PreferencesUtil preferencesUtil;
    private AudioManager audioManager;
    private final Context ctx;


    public MusicManager(Context context) {
        this.ctx = context;
        globalsUtil = (GlobalsUtil) ctx.getApplicationContext();
        broadcastUtils = new BroadcastUtils(globalsUtil.getContext());
        preferencesUtil = new PreferencesUtil(globalsUtil.getContext());
    }

    public void initMediaPlayer() {
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

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(ctx, PowerManager.PARTIAL_WAKE_LOCK);

        try {
            mediaPlayer.setDataSource(globalsUtil.activeAudio.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            globalsUtil.musicService.stopSelf();
        }

        PreferencesUtil initStatus = new PreferencesUtil(ctx);

        if (!initStatus.GetFirstInit()) {
            initStatus.firstInit(true);
        }

        mediaPlayer.prepareAsync();
    }


    private void Play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateCurrentPosition(PlaybackState.STATE_PLAYING);
        }
    }

    private void Pause() {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateCurrentPosition(PlaybackState.STATE_PAUSED);

            globalsUtil.resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void Stop() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void Skip() {
        if (globalsUtil.audioIndex == globalsUtil.songList.size() -1) {
            globalsUtil.audioIndex = 0;
            globalsUtil.activeAudio = globalsUtil.songList.get(globalsUtil.audioIndex);
        } else {
            globalsUtil.activeAudio = globalsUtil.songList.get(++globalsUtil.audioIndex);
            Log.d("new playing", "" + globalsUtil.activeAudio.getTitle());
        }

        preferencesUtil.storeAudioIndex(globalsUtil.audioIndex);
        preferencesUtil.SetLastIndex(globalsUtil.audioIndex);

        Stop();
        mediaPlayer.reset();
        initMediaPlayer();
        broadcastUtils.playbackNotification(BroadcastConstants.UpdateMetaData, GlobalsUtil.Status.PLAYING);
    }

    private void Previous() {

        if (globalsUtil.audioIndex == 0) {
                    globalsUtil.audioIndex = globalsUtil.songList.size() -1;
                    globalsUtil.activeAudio = globalsUtil.songList.get(globalsUtil.audioIndex);
        } else {
                    globalsUtil.activeAudio = globalsUtil.songList.get(--globalsUtil.audioIndex);
        }

        preferencesUtil.storeAudioIndex(globalsUtil.audioIndex);
        preferencesUtil.SetLastIndex(globalsUtil.audioIndex);

        mediaPlayer.reset();
        initMediaPlayer();
        broadcastUtils.playbackNotification(BroadcastConstants.UpdateMetaData, GlobalsUtil.Status.PLAYING);
    }

    private void SeekTo(int seek) {
        if (mediaPlayer == null) return;
        mediaPlayer.seekTo(seek);
        updateCurrentPosition(PlaybackState.STATE_PLAYING);
    }

    public void Resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(globalsUtil.resumePosition);
            mediaPlayer.start();

            updateCurrentPosition(PlaybackState.STATE_PLAYING);
        }
    }

    public void Reset() {
        if(globalsUtil.isServiceBound()) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.reset();
            }
        }
    }

    public void Destroy() {
        Log.d("Destroy()", "called");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    private void updateCurrentPosition(int state) {
        Log.e("called", "media update called");
        if (mediaPlayer != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("called", "media update called");
                    int currentPosition = 0;
                    if (globalsUtil.isServiceBound()) {
                        if (mediaPlayer == null) return;
                        currentPosition = mediaPlayer.getCurrentPosition();
                    }

                    PlaybackState playbackState =
                            new PlaybackState.Builder()
                                    .setState(state, currentPosition, 1)
                                    .setActions(PlaybackState.ACTION_SEEK_TO)
                                    .build();

                    globalsUtil.mediaSession.setPlaybackState(playbackState);
                }
            }, 50);
        }
    }


    public int getCurrentPosition() {
        if (mediaPlayer != null)return mediaPlayer.getCurrentPosition();
        return 0;
    }

    public int getTotalDuration() {
        if (mediaPlayer != null) return mediaPlayer.getDuration();
        return 0;
    }

    public boolean getPlayingState() {
        if (mediaPlayer != null) return mediaPlayer.isPlaying();
        return false;
    }

    public int getMediaSession() {
        if (mediaPlayer != null) return mediaPlayer.getAudioSessionId();
        return -1;
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
                broadcastUtils.playbackFilter(BroadcastConstants.RequestInit)
        );
    }

    private void RegisterPlay() {
        ctx.registerReceiver(playAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestPlay)
        );
    }
    private void RegisterPause() {
        ctx.registerReceiver(pauseAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestPause)
        );
    }
    private void RegisterStop() {
        ctx.registerReceiver(stopAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestStop)
        );
    }
    private void RegisterSkip() {
        ctx.registerReceiver(skipAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestSkip)
        );
    }
    private void RegisterPrev() {
        ctx.registerReceiver(prevAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestPrev)
        );
    }
    private void RegisterResume() {
        ctx.registerReceiver(resumeAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestResume)
        );
    }

    private void RegisterSeek() {
        ctx.registerReceiver(seekAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestSeek)
        );
    }

    private void RegisterReset() {
        ctx.registerReceiver(ResetAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestReset)
        );
    }

    private void RegisterDestroy() {
        ctx.registerReceiver(DestroyAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestDestroy));
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

    /**
     * controle de apps
     * @param focusChange
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                /**
                 * Resumir a reprodução
                 */
                if (mediaPlayer == null) initMediaPlayer();
                else if(!mediaPlayer.isPlaying()) Resume();

                mediaPlayer.setVolume(1.0f, 1.0f);
                broadcastUtils
                        .playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);
            break;
            case AudioManager.AUDIOFOCUS_LOSS:
                /**
                 * Apps que reproduzem por um longo tempo..
                 * Quando o usuario mudar de app.
                 * Permitir que ele volte a reproduzir.
                 */
                if (mediaPlayer.isPlaying()) Pause();
                broadcastUtils
                        .playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PAUSED);

                broadcastUtils
                        .playbackUIManager(BroadcastConstants.RequestPlayChange, false);
            break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                /**
                 * Parar por um curto tempo (parar quando um status/stories com musica começar a tocar
                 * e Resumir quando o status/stories encerrar/fechar)
                 */
                if (mediaPlayer.isPlaying()) Pause();
                broadcastUtils
                        .playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PAUSED);
            break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Quando receber uma notificação, diminua o volume
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
            break;
        }
    }

    public boolean requestAudioFocus() {
        audioManager = (AudioManager)
                ctx.getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    public boolean removeAudioFocus() {
        if (globalsUtil.isServiceBound()) {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    audioManager.abandonAudioFocus(this);
        }
        return false;
    }
}
