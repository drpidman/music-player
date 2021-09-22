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
import android.graphics.Picture;
import android.media.Image;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.listeners.media.MediaSessionCallbacks;
import com.drkryz.musicplayer.screens.MainActivity;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;

import java.io.IOException;

public class NotificationBuilderManager {

    private final GlobalVariables globalVariables;
    private final BroadcastSenders broadcastSenders;
    private final Context ctx;

    public NotificationBuilderManager(Context context) {
        this.ctx = context;
        globalVariables = (GlobalVariables) context.getApplicationContext();
        broadcastSenders = new BroadcastSenders(context);
    }

    public void initMediaSession() throws RemoteException {
        if (globalVariables.mediaSession != null) return;

        globalVariables.mediaSessionManager = (MediaSessionManager)
                ctx.getSystemService(Context.MEDIA_SESSION_SERVICE);

        globalVariables.mediaSession = new MediaSession(ctx, "AudioPlayer");
        globalVariables.transportControls = globalVariables.mediaSession.getController().getTransportControls();

        globalVariables.mediaSession.setActive(true);

        globalVariables.mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        globalVariables.mediaSession.setCallback(new MediaSessionCallbacks(ctx));

        PlaybackState playbackState =
                new PlaybackState.Builder()
                        .setState(PlaybackState.STATE_PAUSED, 0, 1)
                        .setActions(PlaybackState.ACTION_SEEK_TO)
                        .build();

        globalVariables.mediaSession.setPlaybackState(playbackState);
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
        Log.d("updateMetaData", "" + globalVariables.activeAudio);

        Uri url = Uri.parse(globalVariables.activeAudio.getAlbum());

        Bitmap albumArt = null;
        try {
            albumArt = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), url);
        } catch (IOException e) {
            albumArt = BitmapFactory.decodeResource(
                    ctx.getResources(),
                    R.drawable.default_music
            );
        }

        Long duration = Long.parseLong(globalVariables.activeAudio.getDuration());

        globalVariables.mediaSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, globalVariables.activeAudio.getAuthor())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, globalVariables.activeAudio.getTitle())
                .putString(MediaMetadata.METADATA_KEY_TITLE, globalVariables.activeAudio.getTitle())
                .putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(globalVariables.activeAudio.getDuration()))
                .build());

    }

    @SuppressLint("ServiceCast")
    private void buildNotification(GlobalVariables.Status status) {
        int notificationAction = R.drawable.ic_baseline_pause;
        PendingIntent playAction_PauseAction = null;

        if (status == GlobalVariables.Status.PLAYING) {
            notificationAction = R.drawable.ic_baseline_pause;
            playAction_PauseAction = playbackAction(1);
        } else if (status == GlobalVariables.Status.PAUSED) {
            notificationAction = R.drawable.ic_baseline_play;
            playAction_PauseAction = playbackAction(0);
        }

        Uri url = Uri.parse(globalVariables.activeAudio.getAlbum());

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
                                    .setMediaSession(globalVariables.mediaSession.getSessionToken())
                            )
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setLargeIcon(largeIcon)
                            .setSmallIcon(android.R.drawable.stat_sys_headset)
                            .setColor(globalVariables.getResources().getColor(android.R.color.holo_purple))
                            .setContentText(globalVariables.activeAudio.getAuthor())
                            .setContentTitle(globalVariables.activeAudio.getTitle())
                            .setContentInfo(globalVariables.activeAudio.getTitle())
                            .setCategory(Notification.CATEGORY_SERVICE)
                            .addAction(R.drawable.ic_baseline_previous, "previous", playbackAction(3))
                            .addAction(notificationAction, "pause", playAction_PauseAction)
                            .addAction(R.drawable.ic_baseline_skip, "next", playbackAction(2));

            ((NotificationManager) globalVariables.getSystemService(Context.NOTIFICATION_SERVICE)).notify(145, mBuilder.build());
        } else {

            MediaSession.Token token = globalVariables.mediaSession.getSessionToken();

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
                            .setColor(globalVariables.getResources().getColor(android.R.color.holo_purple))
                            .setContentText(globalVariables.activeAudio.getAuthor())
                            .setContentTitle(globalVariables.activeAudio.getTitle())
                            .setContentInfo(globalVariables.activeAudio.getTitle())
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
        GlobalVariables.Status status =
                (GlobalVariables.Status) intent.getSerializableExtra("status");

        if (status == GlobalVariables.Status.PLAYING) {
            buildNotification(GlobalVariables.Status.PLAYING);
        } else {
            buildNotification(GlobalVariables.Status.PAUSED);
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
                broadcastSenders.playbackFilter(BroadcastConstants.RequestNotification)
        );
    }

    private void RegisterUpdater() {
        ctx.registerReceiver(notificationUpdateAction,
                broadcastSenders.playbackFilter(BroadcastConstants.UpdateMetaData)
        );
    }

    private void RegisterRemove() {
        ctx.registerReceiver(removeNotificationAction,
                broadcastSenders.playbackFilter(BroadcastConstants.RemoveNotification)
        );
    }

    public void registerAll() {
        Log.d(globalVariables.getPackageName(), "All senders registered");
        RegisterNotification();
        RegisterUpdater();
        RegisterRemove();
    }

    public void unregisterAll() {
        Log.d(globalVariables.getPackageName(), "All senders canceled");
        ctx.unregisterReceiver(buildNotificationAction);
        ctx.unregisterReceiver(notificationUpdateAction);
        ctx.unregisterReceiver(removeNotificationAction);

        removeNotification();
    }
}