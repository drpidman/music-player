package com.drkryz.musicplayer.listeners.media;

import android.content.Context;
import android.media.session.MediaSession;
import android.os.Build;

import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.managers.NotificationBuilderManager;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

public class MediaSessionCallbacks extends MediaSession.Callback {

    private final GlobalsUtil globalsUtil;
    private final BroadcastUtils broadcastUtils;
    private final PreferencesUtil preferencesUtil;
    private final NotificationBuilderManager notificationBuilderManager;


    public MediaSessionCallbacks(Context context) {
        globalsUtil = (GlobalsUtil) context.getApplicationContext();
        broadcastUtils = new BroadcastUtils(context);
        preferencesUtil = new PreferencesUtil(context);
        notificationBuilderManager = new NotificationBuilderManager(context);
    }

    @Override
    public void onPlay() {
        super.onPlay();
        globalsUtil.musicService.getMusicManager().Resume();

        notificationBuilderManager.buildNotification(GlobalsUtil.Status.PLAYING);

        if (!preferencesUtil.LoadUserInApp()) return;
        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);
        preferencesUtil.StorePlayingState(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        globalsUtil.musicService.getMusicManager().Pause();

        notificationBuilderManager.buildNotification(GlobalsUtil.Status.PAUSED);

        if (!preferencesUtil.LoadUserInApp()) return;
        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, false);
        preferencesUtil.StorePlayingState(false);
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        globalsUtil.musicService.getMusicManager().Skip();
        notificationBuilderManager.buildNotification(GlobalsUtil.Status.PLAYING);

        preferencesUtil.StorePlayingState(true);
        preferencesUtil.clearCover();

        if (!preferencesUtil.LoadUserInApp()) return;
        notificationBuilderManager.buildNotification(GlobalsUtil.Status.PLAYING);
        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        globalsUtil.musicService.getMusicManager().Previous();
        notificationBuilderManager.buildNotification(GlobalsUtil.Status.PLAYING);

        preferencesUtil.StorePlayingState(true);
        preferencesUtil.clearCover();

        if (!preferencesUtil.LoadUserInApp()) return;
        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);
    }

    @Override
    public void onStop() {
        super.onStop();
        notificationBuilderManager.buildNotification(GlobalsUtil.Status.PAUSED);
        globalsUtil.musicService.getMusicManager().Stop();
    }

    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            globalsUtil.musicService.getMusicManager().SeekTo(Math.toIntExact(pos));
        } else {
            String positionString = String.valueOf(pos);
            globalsUtil.musicService.getMusicManager().SeekTo(Integer.parseInt(positionString));
        }

        if (!preferencesUtil.LoadUserInApp()) return;
        broadcastUtils.playbackUIManager(BroadcastConstants.RequestPlayChange, true);
    }
}