package com.drkryz.musicplayer.listeners.media;

import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;

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
        Log.e("an error as", ""+ i);
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
                .playbackManager(BroadcastConstants.RequestPlay);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // auto play
        broadcastSenders.playbackManager(BroadcastConstants.RequestSkip);
        broadcastSenders.playbackNotification(BroadcastConstants.RemoveNotification, GlobalVariables.Status.PLAYING);
        broadcastSenders.playbackNotification(BroadcastConstants.UpdateMetaData, null);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }
}
