package com.drkryz.musicplayer.listeners.media;

import android.content.Context;
import android.media.session.MediaSession;
import android.support.v4.media.session.MediaSessionCompat;

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
        broadcastSenders.playbackManager(BroadcastConstants.RequestPlay);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }

    @Override
    public void onPause() {
        super.onPause();
        broadcastSenders.playbackManager(BroadcastConstants.RequestPause);
        broadcastSenders.playbackManager(BroadcastConstants.RequestNotification);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PAUSED);
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        broadcastSenders.playbackManager(BroadcastConstants.RequestSkip);
        broadcastSenders.playbackManager(BroadcastConstants.RequestNotification);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        broadcastSenders.playbackManager(BroadcastConstants.RequestPrev);
        broadcastSenders.playbackManager(BroadcastConstants.RequestNotification);
        broadcastSenders.playbackNotification(BroadcastConstants.RequestNotification, GlobalVariables.Status.PLAYING);
    }

    @Override
    public void onStop() {
        super.onStop();
        broadcastSenders.playbackManager(BroadcastConstants.RequestStop);
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
    }
}
