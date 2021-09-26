package com.drkryz.musicplayer.listeners.media;

import android.content.Context;
import android.media.session.MediaSession;

import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;

public class MediaSessionCallbacks extends MediaSession.Callback {

    private final GlobalsUtil globalsUtil;
    private final BroadcastUtils broadcastUtils;

    public MediaSessionCallbacks(Context context) {
        globalsUtil = (GlobalsUtil) context.getApplicationContext();
        broadcastUtils = new BroadcastUtils(context);
    }

    @Override
    public void onPlay() {
        super.onPlay();
        broadcastUtils.playbackManager(BroadcastConstants.RequestResume, 0);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);

        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        broadcastUtils.playbackManager(BroadcastConstants.RequestPause, 0);
        broadcastUtils.playbackManager(BroadcastConstants.RequestNotification, 0);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PAUSED);

        // send to user interface
        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, false);
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        broadcastUtils.playbackManager(BroadcastConstants.RequestSkip, 0);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        broadcastUtils.playbackManager(BroadcastConstants.RequestPrev, 0);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);
    }

    @Override
    public void onStop() {
        super.onStop();
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PAUSED);
        broadcastUtils.playbackManager(BroadcastConstants.RequestStop, 0);
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
        broadcastUtils.playbackManager(BroadcastConstants.RequestSeek, pos);
    }
}