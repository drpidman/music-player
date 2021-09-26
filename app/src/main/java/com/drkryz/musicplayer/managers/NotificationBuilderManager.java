package com.drkryz.musicplayer.managers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.drkryz.musicplayer.listeners.media.MediaSessionCallbacks;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;

import java.io.IOException;

public class NotificationBuilderManager {

    private final GlobalsUtil globalsUtil;
    private final BroadcastUtils broadcastUtils;
    private final Context ctx;

    public NotificationBuilderManager(Context context) {
        this.ctx = context;
        globalsUtil = (GlobalsUtil) context.getApplicationContext();
        broadcastUtils = new BroadcastUtils(context);
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

    private Bitmap createBitmap(String image) {
        try {
            byte[] encodeByte= Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    private void updateMetaData() {
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

    @SuppressLint("ServiceCast")
    private void buildNotification(GlobalsUtil.Status status) {
        int notificationAction = R.drawable.ic_baseline_pause;
        PendingIntent playAction_PauseAction = null;

        if (status == GlobalsUtil.Status.PLAYING) {
            notificationAction = R.drawable.ic_baseline_pause;
            playAction_PauseAction = playbackAction(1);
        } else if (status == GlobalsUtil.Status.PAUSED) {
            notificationAction = R.drawable.ic_baseline_play;
            playAction_PauseAction = playbackAction(0);
        }

        Uri url = Uri.parse(globalsUtil.activeAudio.getAlbum());

        Bitmap largeIcon = null;
        try {
            largeIcon = MediaStore.Images.Media.getBitmap(
                    ctx.getContentResolver(),
                    url
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


        Notification.Builder mBuilder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = (Notification.Builder)
                    new Notification.Builder(ctx, "Music Player")
                            .setShowWhen(true)
                            .setOnlyAlertOnce(true)
                            .setStyle(new Notification.MediaStyle()
                                    .setShowActionsInCompactView(0, 1, 2)
                                    .setMediaSession(globalsUtil.mediaSession.getSessionToken())
                            )
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setLargeIcon(largeIcon)
                            .setSmallIcon(android.R.drawable.stat_sys_headset)
                            .setColor(globalsUtil.getResources().getColor(android.R.color.holo_purple))
                            .setContentText(globalsUtil.activeAudio.getAuthor())
                            .setContentTitle(globalsUtil.activeAudio.getTitle())
                            .setContentInfo(globalsUtil.activeAudio.getTitle())
                            .setCategory(Notification.CATEGORY_SERVICE)
                            .addAction(R.drawable.ic_baseline_previous, "previous", playbackAction(3))
                            .addAction(notificationAction, "pause", playAction_PauseAction)
                            .addAction(R.drawable.ic_baseline_skip, "next", playbackAction(2));

            ((NotificationManager) globalsUtil.getSystemService(Context.NOTIFICATION_SERVICE)).notify(145, mBuilder.build());
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
                            .setShowWhen(true)
                            .setOnlyAlertOnce(true)
                            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                            .setLargeIcon(largeIcon)
                            .setSmallIcon(android.R.drawable.stat_sys_headset)
                            .setColor(globalsUtil.getResources().getColor(android.R.color.holo_purple))
                            .setContentText(globalsUtil.activeAudio.getAuthor())
                            .setContentTitle(globalsUtil.activeAudio.getTitle())
                            .setContentInfo(globalsUtil.activeAudio.getTitle())
                            .setCategory(Notification.CATEGORY_SERVICE)
                            .addAction(R.drawable.ic_baseline_previous, "previous", playbackAction(3))
                            .addAction(notificationAction, "pause", playAction_PauseAction)
                            .addAction(R.drawable.ic_baseline_skip, "next", playbackAction(2));

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);

            notificationManagerCompat.notify(145, notificationCompat.build());
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
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(BroadcastConstants.ACTION_PAUSE);
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(BroadcastConstants.ACTION_SKIP);
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(BroadcastConstants.ACTION_PREV);
                return PendingIntent.getService(context, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void resolveNotificationStatus(Intent intent) {
        GlobalsUtil.Status status =
                (GlobalsUtil.Status) intent.getSerializableExtra("status");

        if (status == GlobalsUtil.Status.PLAYING) {
            buildNotification(GlobalsUtil.Status.PLAYING);
        } else {
            buildNotification(GlobalsUtil.Status.PAUSED);
        }
    }


    private final BroadcastReceiver buildNotificationAction = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReceive(Context context, Intent intent) {
            resolveNotificationStatus(intent);
        }
    };

    private final BroadcastReceiver notificationUpdateAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateMetaData();
        }
    };

    private final BroadcastReceiver removeNotificationAction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeNotification();
        }
    };

    private void RegisterNotification() {
        ctx.registerReceiver(buildNotificationAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RequestNotification)
        );
    }

    private void RegisterUpdater() {
        ctx.registerReceiver(notificationUpdateAction,
                broadcastUtils.playbackFilter(BroadcastConstants.UpdateMetaData)
        );
    }

    private void RegisterRemove() {
        ctx.registerReceiver(removeNotificationAction,
                broadcastUtils.playbackFilter(BroadcastConstants.RemoveNotification)
        );
    }

    public void registerAll() {
        Log.d(globalsUtil.getPackageName(), "All senders registered");
        RegisterNotification();
        RegisterUpdater();
        RegisterRemove();
    }

    public void unregisterAll() {
        Log.d(globalsUtil.getPackageName(), "All senders canceled");
        ctx.unregisterReceiver(buildNotificationAction);
        ctx.unregisterReceiver(notificationUpdateAction);
        ctx.unregisterReceiver(removeNotificationAction);

        removeNotification();
    }
}