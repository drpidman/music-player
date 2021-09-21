package com.drkryz.musicplayer.listeners.media;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.media.AudioManagerCompat;

import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;
import com.drkryz.musicplayer.utils.StorageUtil;

public class MusicListeners implements
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener
{

    private final GlobalVariables globalVariables;
    private final BroadcastSenders broadcastSenders;

    public MusicListeners(Context context) {
        globalVariables = (GlobalVariables) context.getApplicationContext();
        broadcastSenders = new BroadcastSenders(context);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + i1);
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

        new BroadcastSenders(globalVariables.getContext())
                .playbackManager(BroadcastConstants.RequestPlay, 0);

        new BroadcastSenders(globalVariables.getContext())
                .playbackUIManager(BroadcastConstants.UpdateCover, false);

    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d("onCompletion()", "media completed");

        // change: broadcastSenders.playbackManager(...) to transportControls.skipToNext();
        // fix auto playing
        globalVariables.transportControls.skipToNext();
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }
}
