package com.drkryz.musicplayer.listeners.media;

import android.content.Context;
import android.media.session.MediaSession;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;

public class MediaSessionCallbacks extends MediaSession.Callback {

    private final GlobalVariables globalVariables;
    private final BroadcastSenders broadcastSenders;

    public MediaSessionCallbacks(Context context) {
        globalVariables = (GlobalVariables) context.getApplicationContext();
        broadcastSenders = new BroadcastSenders(context);
    }

    @Override
    public void onPlay() {
        super.onPlay();
        broadcastSenders.playbackManager(BroadcastConstants.RequestResume, 0);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);

        broadcastSenders.playbackUIManager(BroadcastConstants.RequestPlayChange, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        broadcastSenders.playbackManager(BroadcastConstants.RequestPause, 0);
        broadcastSenders.playbackManager(BroadcastConstants.RequestNotification, 0);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PAUSED);

        // send to user interface
        broadcastSenders.playbackUIManager(BroadcastConstants.RequestPlayChange, false);
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        broadcastSenders.playbackManager(BroadcastConstants.RequestSkip, 0);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        broadcastSenders.playbackManager(BroadcastConstants.RequestPrev, 0);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }

    @Override
    public void onStop() {
        super.onStop();
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PAUSED);
        broadcastSenders.playbackManager(BroadcastConstants.RequestStop, 0);
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
        broadcastSenders.playbackManager(BroadcastConstants.RequestSeek, pos);
    }
}