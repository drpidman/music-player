package com.drkryz.musicplayer.managers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.PlaybackAlbum;
import com.drkryz.musicplayer.listeners.media.MediaSessionCallbacks;
import com.drkryz.musicplayer.screens.PlayerActivity;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NotificationBuilderManager {

    private final GlobalsUtil globalsUtil;
    private final Context ctx;

    static int FLAGS = 0;

    public NotificationBuilderManager(Context context) {
        this.ctx = context;
        globalsUtil = (GlobalsUtil) context.getApplicationContext();
    }

    public void initMediaSession() throws RemoteException {
        if (globalsUtil.mediaSession != null) return;

        globalsUtil.mediaSessionManager = (MediaSessionManager)
                ctx.getSystemService(Context.MEDIA_SESSION_SERVICE);

        globalsUtil.mediaSession = new MediaSession(ctx, "AudioPlayer");
        globalsUtil.transportControls = globalsUtil.mediaSession.getController().getTransportControls();

        globalsUtil.mediaSession.setActive(true);

        globalsUtil.mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        globalsUtil.mediaSession.setCallback(new MediaSessionCallbacks(ctx));

        PlaybackState playbackState =
                new PlaybackState.Builder()
                        .setState(PlaybackState.STATE_PAUSED, 0, 1)
                        .setActions(PlaybackState.ACTION_SEEK_TO)
                        .build();

        globalsUtil.mediaSession.setPlaybackState(playbackState);
    }

    public void updateMetaData() {
        Log.d("updateMetaData", "" + globalsUtil.activeAudio);

        Uri url = Uri.parse(globalsUtil.activeAudio.getAlbum());

        Bitmap albumArt = null;
        try {
            albumArt = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), url);
        } catch (IOException e) {
            albumArt = BitmapFactory.decodeResource(
                    ctx.getResources(),
                    R.drawable.default_music
            );
        }

        Long duration = Long.parseLong(globalsUtil.activeAudio.getDuration());

        globalsUtil.mediaSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, globalsUtil.activeAudio.getAuthor())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, globalsUtil.activeAudio.getTitle())
                .putString(MediaMetadata.METADATA_KEY_TITLE, globalsUtil.activeAudio.getTitle())
                .putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(globalsUtil.activeAudio.getDuration()))
                .build());

    }

    private static Bitmap largeIcon = null;

    @SuppressLint("ServiceCast")
    public void buildNotification(GlobalsUtil.Status status, Service service) {
        int notificationAction = R.drawable.ui_pause;
        PendingIntent playAction_PauseAction = null;

        if (status == GlobalsUtil.Status.PLAYING) {
            notificationAction = R.drawable.ui_pause;
            playAction_PauseAction = playbackAction(1);
        } else if (status == GlobalsUtil.Status.PAUSED) {
            notificationAction = R.drawable.ui_play;
            playAction_PauseAction = playbackAction(0);
        }


        Bitmap cover = PlaybackAlbum.getCover(ctx, globalsUtil.audioIndex, globalsUtil.songList);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cover.compress(Bitmap.CompressFormat.JPEG, 50, out);
        Bitmap largeIcon = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        Notification.Builder mBuilder = null;

        Intent mainIntent = new Intent(ctx, PlayerActivity.class);
        PendingIntent mainPending = PendingIntent.getActivity(ctx, 1006, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = (Notification.Builder)
                    new Notification.Builder(ctx, "Music Player")
                            .setShowWhen(true)
                            .setContentIntent(mainPending)
                            .setOngoing(new PreferencesUtil(ctx).GetPlayingState())
                            .setOnlyAlertOnce(true)
                            .setStyle(new Notification.MediaStyle()
                                    .setShowActionsInCompactView(0, 1, 2)
                                    .setMediaSession(globalsUtil.mediaSession.getSessionToken())
                            )
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setLargeIcon(largeIcon)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentText(globalsUtil.activeAudio.getAuthor())
                            .setContentTitle(globalsUtil.activeAudio.getTitle())
                            .setContentInfo(globalsUtil.activeAudio.getTitle())
                            .setCategory(Notification.CATEGORY_SERVICE)
                            .addAction(R.drawable.ui_prev, "previous", playbackAction(3))
                            .addAction(notificationAction, "pause", playAction_PauseAction)
                            .addAction(R.drawable.ui_next, "next", playbackAction(2))
                            .addAction(R.drawable.ic_baseline_close_24, "close", playbackAction(4));

            service.startForeground(145, mBuilder.build());
        } else {

            MediaSession.Token token = globalsUtil.mediaSession.getSessionToken();

            androidx.core.app.NotificationCompat.Builder notificationCompat
                    = (androidx.core.app.NotificationCompat.Builder)
                    new androidx.core.app.NotificationCompat.Builder(ctx,
                            "Music Player"
                    )
                            .setStyle(new MediaStyle()
                                    .setMediaSession(MediaSessionCompat.Token.fromToken(token))
                                    .setShowActionsInCompactView(0,1,2)
                            )
                            .setContentIntent(mainPending)
                            .setShowWhen(true)
                            .setOnlyAlertOnce(true)
                            .setOngoing(new PreferencesUtil(ctx).GetPlayingState())
                            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                            .setLargeIcon(largeIcon)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setColor(globalsUtil.getResources().getColor(android.R.color.holo_purple))
                            .setContentText(globalsUtil.activeAudio.getAuthor())
                            .setContentTitle(globalsUtil.activeAudio.getTitle())
                            .setContentInfo(globalsUtil.activeAudio.getTitle())
                            .setCategory(Notification.CATEGORY_SERVICE)
                            .addAction(R.drawable.ui_prev, "previous", playbackAction(3))
                            .addAction(notificationAction, "pause", playAction_PauseAction)
                            .addAction(R.drawable.ui_next, "next", playbackAction(2))
                            .addAction(R.drawable.ic_baseline_close_24, "close", playbackAction(4));
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);

            service.startForeground(145, notificationCompat.build());
        }
    }

    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(145);
    }


    private PendingIntent playbackAction(int actionNumber) {
        Context context = ctx;
        Intent playbackAction = new Intent(context, MusicService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(BroadcastConstants.ACTION_PLAY);
                return PendingIntent.getService(context, actionNumber, playbackAction, FLAGS);
            case 1:
                // Pause
                playbackAction.setAction(BroadcastConstants.ACTION_PAUSE);
                return PendingIntent.getService(context, actionNumber, playbackAction, FLAGS);
            case 2:
                // Next track
                playbackAction.setAction(BroadcastConstants.ACTION_SKIP);
                return PendingIntent.getService(context, actionNumber, playbackAction, FLAGS);
            case 3:
                // Previous track
                playbackAction.setAction(BroadcastConstants.ACTION_PREV);
                return PendingIntent.getService(context, actionNumber, playbackAction, FLAGS);
            default:
                break;
            case 4:
                playbackAction.setAction(BroadcastConstants.ACTION_CLOSE);
                return PendingIntent.getService(context, actionNumber, playbackAction, FLAGS);
        }
        return null;
    }
}