package com.drkryz.musicplayer.listeners.media;

import android.content.Context;
import android.media.session.MediaSession;

import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

public class MediaSessionCallbacks extends MediaSession.Callback {

    private final GlobalsUtil globalsUtil;
    private final BroadcastUtils broadcastUtils;

    private final PreferencesUtil preferencesUtil;

    public MediaSessionCallbacks(Context context) {
        globalsUtil = (GlobalsUtil) context.getApplicationContext();
        broadcastUtils = new BroadcastUtils(context);

        preferencesUtil = new PreferencesUtil(context);
    }

    @Override
    public void onPlay() {
        super.onPlay();
        broadcastUtils.playbackManager(BroadcastConstants.RequestResume, 0);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);
        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);

        preferencesUtil.StorePlayingState(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        broadcastUtils.playbackManager(BroadcastConstants.RequestPause, 0);
        broadcastUtils.playbackManager(BroadcastConstants.RequestNotification, 0);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PAUSED);
        // send to user interface
        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, false);
        preferencesUtil.StorePlayingState(false);
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        broadcastUtils.playbackManager(BroadcastConstants.RequestSkip, 0);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);

        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);
        preferencesUtil.StorePlayingState(true);
        preferencesUtil.SetLastIndex(globalsUtil.audioIndex);
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        broadcastUtils.playbackManager(BroadcastConstants.RequestPrev, 0);
        broadcastUtils.playbackNotification(BroadcastConstants.RequestNotification, GlobalsUtil.Status.PLAYING);

        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);

        preferencesUtil.StorePlayingState(true);
        preferencesUtil.SetLastIndex(globalsUtil.audioIndex);
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

        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);
    }
}