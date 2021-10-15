package com.drkryz.musicplayer.managers;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

import java.io.IOException;


public class MusicManager
        implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener
{

    private MediaPlayer mediaPlayer;
    private final GlobalsUtil globalsUtil;
    private final BroadcastUtils broadcastUtils;
    private final PreferencesUtil preferencesUtil;
    private AudioManager audioManager;
    private final Context ctx;

    private NotificationBuilderManager notificationBuilderManager;


    public MusicManager(Context context) {
        this.ctx = context;
        globalsUtil = (GlobalsUtil) ctx.getApplicationContext();
        broadcastUtils = new BroadcastUtils(globalsUtil.getContext());
        preferencesUtil = new PreferencesUtil(globalsUtil.getContext());
        notificationBuilderManager = new NotificationBuilderManager(globalsUtil.getContext());
    }

    public void initMediaPlayer() {
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);

        mediaPlayer.reset();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        );

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        PowerManager powerManager = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                ctx.getPackageName() + ":" + "wakelock"
                );

        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        try {
            mediaPlayer.setDataSource(globalsUtil.activeAudio.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            globalsUtil.stopService(new Intent(globalsUtil.getContext(), MusicService.class));
        }

        PreferencesUtil initStatus = new PreferencesUtil(ctx);

        if (!initStatus.GetFirstInit()) {
            initStatus.firstInit(true);
        }

        mediaPlayer.prepareAsync();
    }


    public void Play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateCurrentPosition(PlaybackState.STATE_PLAYING);
        }
    }

    public void Pause() {
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

    public void Skip() {
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
        notificationBuilderManager.updateMetaData();
    }

    public void Previous() {

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
        notificationBuilderManager.updateMetaData();
    }

    public void SeekTo(int seek) {
        if (mediaPlayer == null) return;
        mediaPlayer.seekTo(seek);
        updateCurrentPosition(PlaybackState.STATE_PLAYING);
    }

    public void Resume() {
        if (mediaPlayer == null) return;
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(globalsUtil.resumePosition);
            mediaPlayer.start();

            updateCurrentPosition(PlaybackState.STATE_PLAYING);
        }
    }

    public void Reset() {
        if(globalsUtil.isServiceBound()) {
            if (mediaPlayer == null) return;
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

            new PreferencesUtil(ctx).StorePlayingState(false);
            new PreferencesUtil(ctx).StoreUserInApp(false);


            globalsUtil.stopService(new Intent(ctx, MusicService.class));
            globalsUtil.musicService.stopForeground(true);

            android.os.Process.killProcess(Process.myPid());
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


    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + i1);
                Toast.makeText(globalsUtil.getContext(), "PROGRESSIVE_PLAYBACK", Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + i1);
                Toast.makeText(globalsUtil.getContext(), "SERVER_DIED", Toast.LENGTH_SHORT).show();

                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + i1);
                Toast.makeText(globalsUtil.getContext(), "ERROR UNKNOWN", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        // play

        Play();

        if (!preferencesUtil.LoadUserInApp()) return;

        broadcastUtils
                .playbackUIManager(BroadcastConstants.UpdateCover, mediaPlayer.isPlaying());

        broadcastUtils
                .playbackUIManager(BroadcastConstants.RequestProgress, false);

    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d("onCompletion()", "media completed");
        Skip();
        notificationBuilderManager.buildNotification(GlobalsUtil.Status.PLAYING, globalsUtil.musicService);
    }


    public int getCurrentPosition() {
        if (mediaPlayer != null) return mediaPlayer.getCurrentPosition();
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
                notificationBuilderManager.buildNotification(GlobalsUtil.Status.PLAYING, globalsUtil.musicService);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                /**
                 * Apps que reproduzem por um longo tempo..
                 * Quando o usuario mudar de app.
                 * Permitir que ele volte a reproduzir.
                 */
                if (mediaPlayer.isPlaying()) Pause();
                notificationBuilderManager.buildNotification(GlobalsUtil.Status.PAUSED, globalsUtil.musicService);

                broadcastUtils
                        .playbackUIManager(BroadcastConstants.RequestPlayChange, false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                /**
                 * Parar por um curto tempo (parar quando um status/stories com musica começar a tocar
                 * e Resumir quando o status/stories encerrar/fechar)
                 */
                if (mediaPlayer.isPlaying()) Pause();
                notificationBuilderManager.buildNotification(GlobalsUtil.Status.PAUSED, globalsUtil.musicService);
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