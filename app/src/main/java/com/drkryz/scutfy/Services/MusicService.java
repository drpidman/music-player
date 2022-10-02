package com.drkryz.scutfy.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController.TransportControls;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.app.NotificationCompat;

import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.drkryz.scutfy.Constants.BroadcastConstants;
import com.drkryz.scutfy.Helpers.UserFavoritesHelper;
import com.drkryz.scutfy.R;
import com.drkryz.scutfy.Screens.MusicActivity;
import com.drkryz.scutfy.Utils.ApplicationUtil;
import com.drkryz.scutfy.Utils.ContentManagerUtil;
import com.drkryz.scutfy.Utils.MediaMetadataUtil;
import com.drkryz.scutfy.Utils.PreferencesUtil;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

public class MusicService extends Service implements
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener

{

    private final IBinder iBinder = new LocalBinder();
    private final String packageName = "com.drkryz.musicplayer";
    public final String LOOPING_CMD = packageName + ".looping";
    public final String SHUFFLE_CMD = packageName + ".shuffle";
    public final String ACTION_FAVORITE = packageName + "ACTION.FAVORITE";
    public final String ACTION_FAVORITE_UNDO = packageName + "ACTION_UNFAVORITE";
    private final String PREV_CMD = packageName + ".previous";
    private final String SKIP_CMD = packageName + ".skip";
    private final String STOP_CMD = packageName + ".stop";
    private final String PLAY_CMD = packageName + ".play";
    private final String INIT_CMD = packageName + ".init";
    private final String SEEK_CMD = packageName + ".seek";
    private final String PAUSE_CMD = packageName + ".pause";
    private final String RESET_CMD = packageName + ".reset";
    private final String RESUME_CMD = packageName + ".resume";
    private final String PREPARE_CMD = packageName + ".prepare";
    private final String FAVORITE_CMD = packageName + ".favorite";
    private final String ON_RESUME_CMD = packageName + ".on.user.resume";
    private final String UI_PLAY = packageName + ".ui.play";
    private final String UI_PAUSE = packageName + ".ui.pause";
    private final String UI_SKIP = packageName + ".ui.skip";
    private final String UI_PREV = packageName + ".ui.previous";
    private final String UI_RESUME = packageName + ".ui.resume";
    private final String UI_UPDATE_MEDIA_CONTROL_BUTTON = packageName + ".ui.update_media_control_button";
    private final String UI_UPDATE_MEDIA_PROGRESS = packageName + "ui.update_media_progress";
    private final String UI_UPDATE_MEDIA_METADATA = packageName + "ui_update_media_metadata";
    private final String NOTIFICATION_CREATE = packageName + ".notification.create";
    private final String NOTIFICATION_REMOVE = packageName + ".notification.remove";
    private final String NOTIFICATION_UPDATE_METADATA = packageName + ".notification.update_metadata";
    private final String ACTION_PLAY = packageName + ".ACTION_PLAY";
    private final String ACTION_PAUSE = packageName + ".ACTION_PAUSE";
    private final String ACTION_STOP = packageName + ".ACTION_STOP";
    private final String ACTION_SKIP = packageName + ".ACTION_SKIP";
    private final String ACTION_PREV = packageName + ".ACTION_PREVIOUS";
    private final String ACTION_CLOSE = packageName + ".ACTION_CLOSE";

    private int audioIndex = -1;
    private String MEDIA_ACTIVE_TITLE = "";

    private UserPlaylist activeAudio;
    private ArrayList<UserPlaylist> musicList;
    private int resumePosition = 0;
    private boolean ongoingCall = false;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private MediaSession mediaSession;
    private PreferencesUtil preferencesUtil;
    private TransportControls transportControls;
    private MediaSessionManager mediaSessionManager;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private PlaybackState playbackState;



    private ContentManagerUtil contentManagerUtil;


    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PauseCommand();
            buildNotification(ApplicationUtil.Status.PAUSED);
            preferencesUtil.storePlayingState(false);
        }
    };
    private boolean resume = false;

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("onBind()", "bind()");
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(getPackageName(), "onUnbind():Service");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.e(getPackageName(), "onRebind():Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferencesUtil = new PreferencesUtil(getBaseContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(getPackageName(), "onDestroy()");

        if (mediaPlayer != null) {

            // preload
            preferencesUtil.setLastPosition(mediaPlayer.getCurrentPosition());

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;


            preferencesUtil.storeAudioIndex(audioIndex);
            mediaSession.release();

            removeAudioFocus();
            unregisterReceiver(becomingNoisyReceiver);
            stopForeground(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        Log.e(getPackageName(), "service():action=" + action);
        switch (action) {
            case ON_RESUME_CMD:
                audioIndex = preferencesUtil.loadAudioIndex();
                MEDIA_ACTIVE_TITLE = preferencesUtil.loadAudioTitle();

                if (mediaPlayer == null) resume = true;

                Log.e(getPackageName(), "init():on_resume_cmd");
                try {
                    audioIndex = preferencesUtil.loadAudioIndex();
                    MEDIA_ACTIVE_TITLE = preferencesUtil.loadAudioTitle();

                    if (audioIndex != -1 && audioIndex < musicList.size()) {
                        activeAudio = musicList.get(audioIndex);
                    } else {
                        stopSelf();
                    }
                } catch (NullPointerException e) {
                    stopSelf();
                    e.printStackTrace();
                }

                if (requestAudioFocus()) {
                    stopSelf();
                }

                if (mediaSessionManager == null) {
                    Log.e(getPackageName(), "null:starting new play");
                    try {
                        initMediaSession();
                        initMedia();
                    } catch (RemoteException e) {
                        stopSelf();
                        e.printStackTrace();
                    }
                }
                break;
            case PREPARE_CMD:
                Log.e(getPackageName(), "prepare():service");

                phoneCallListener();

                if (musicList == null) {

                    contentManagerUtil = new ContentManagerUtil(this);
                    musicList = contentManagerUtil.getMusics(this);
                }

                if (mediaPlayer != null) {
                    LocalBroadcastManager
                            .getInstance(this)
                            .sendBroadcastSync(new Intent(PREPARE_CMD + ".running"));
                }

                LocalBroadcastManager
                        .getInstance(this)
                        .sendBroadcastSync(new Intent(PREPARE_CMD));

                registerNoisyState();

                break;
            case INIT_CMD:
                Log.e(getPackageName(), "init():service");
                try {
                    audioIndex = preferencesUtil.loadAudioIndex();
                    MEDIA_ACTIVE_TITLE = preferencesUtil.loadAudioTitle();

                    if (audioIndex != -1 && audioIndex < musicList.size()) {
                        activeAudio = musicList.get(audioIndex);
                    } else {
                        stopSelf();
                    }
                } catch (NullPointerException e) {
                    stopSelf();
                    e.printStackTrace();
                }

                if (requestAudioFocus()) {
                    stopSelf();
                }

                if (mediaSessionManager == null) {
                    Log.e(getPackageName(), "null:starting new play");
                    try {
                        initMediaSession();
                        initMedia();
                    } catch (RemoteException e) {
                        stopSelf();
                        e.printStackTrace();
                    }
                }

                buildNotification(ApplicationUtil.Status.PLAYING);

                break;
            case PLAY_CMD:
                Log.e(getPackageName(), "play():service=" + preferencesUtil.loadAudioIndex());

                String trackData = preferencesUtil.loadAudioTrackData();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    audioIndex = IntStream.range(0, musicList.size())
                            .filter(item -> musicList.get(item).getPath().equals(trackData)).findFirst().getAsInt();
                }


                MEDIA_ACTIVE_TITLE = preferencesUtil.loadAudioTitle();

                if (audioIndex != -1 && audioIndex < musicList.size()) {
                    activeAudio = musicList.get(audioIndex);
                } else {
                    stopSelf();
                }

                mediaPlayer.stop();
                mediaPlayer.reset();
                initMedia();

                updateMetaData();
                buildNotification(ApplicationUtil.Status.PLAYING);
                break;
            case PAUSE_CMD:
                PauseCommand();
                break;
            case RESUME_CMD:
                ResumeCommand();
                break;
            case SKIP_CMD:
                audioIndex = preferencesUtil.loadAudioIndex();
                MEDIA_ACTIVE_TITLE = preferencesUtil.loadAudioTitle();

                if (mediaPlayer == null) {
                    if (audioIndex == musicList.size() - 1) {
                        audioIndex = 0;
                        activeAudio = musicList.get(audioIndex);
                    } else {
                        activeAudio = musicList.get(++audioIndex);
                    }

                    preferencesUtil.storeAudioIndex(audioIndex);
                    preferencesUtil.storeAudioTitle(MEDIA_ACTIVE_TITLE);

                    if (requestAudioFocus()) stopSelf();

                    if (mediaSessionManager == null) {
                        Log.e(getPackageName(), "null:starting new play");
                        try {
                            initMediaSession();
                            initMedia();
                        } catch (RemoteException e) {
                            stopSelf();
                            e.printStackTrace();
                        }
                    }

                    buildNotification(ApplicationUtil.Status.PLAYING);
                } else {
                    SkipCommand();
                }
                break;
            case PREV_CMD:
                audioIndex = preferencesUtil.loadAudioIndex();
                MEDIA_ACTIVE_TITLE = preferencesUtil.loadAudioTitle();

                if (mediaPlayer == null) {
                    if (audioIndex == 0) {
                        audioIndex = musicList.size() - 1;
                        activeAudio = musicList.get(audioIndex);
                    } else {
                        activeAudio = musicList.get(--audioIndex);
                    }

                    preferencesUtil.storeAudioIndex(audioIndex);
                    preferencesUtil.storeAudioTitle(MEDIA_ACTIVE_TITLE);

                    if (requestAudioFocus()) {
                        stopSelf();
                    }

                    if (mediaSessionManager == null) {
                        Log.e(getPackageName(), "null:starting new play");
                        try {
                            initMediaSession();
                            initMedia();
                        } catch (RemoteException e) {
                            stopSelf();
                            e.printStackTrace();
                        }
                    }

                    buildNotification(ApplicationUtil.Status.PLAYING);
                } else {
                    PreviousCommand();
                }
                break;
            case FAVORITE_CMD:
                if (isFavorite(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE))) {
                    RemoveFavoriteCommand(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE));
                } else {
                    AddFavoriteCommand(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE));
                }

                if (mediaPlayer.isPlaying()) {
                    buildNotification(ApplicationUtil.Status.PLAYING);
                } else {
                    buildNotification(ApplicationUtil.Status.PAUSED);
                }
                break;
            case SHUFFLE_CMD:
                if (preferencesUtil.loadShuffleState()) {
                    Collections.sort(musicList, new Comparator<UserPlaylist>() {
                        @Override
                        public int compare(UserPlaylist o1, UserPlaylist o2) {
                            return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                        }
                    });

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        audioIndex = IntStream.range(0, musicList.size())
                                .filter(idx -> musicList.get(idx).getTitle().equals(activeAudio.getTitle())).findFirst().getAsInt();
                    }


                    preferencesUtil.storeAudioIndex(audioIndex);
                    preferencesUtil.storeShuffleState(false);
                } else {
                    Collections.shuffle(musicList);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        audioIndex = IntStream.range(0, musicList.size())
                                        .filter(idx -> musicList.get(idx).getTitle().equals(activeAudio.getTitle())).findFirst().getAsInt();
                    }

                    preferencesUtil.storeAudioIndex(audioIndex);
                    preferencesUtil.storeShuffleState(true);
                }
                break;
            case LOOPING_CMD:
                mediaPlayer.setLooping(!isLooping());

                preferencesUtil.storeLoopState(mediaPlayer.isLooping());
                break;
            case "update.playlist":
                musicList = new ContentManagerUtil(getBaseContext())
                        .getMusics(getBaseContext());


                if (preferencesUtil.loadShuffleState()) {
                    Collections.shuffle(musicList);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        audioIndex = IntStream.range(0, musicList.size())
                                .filter(idx -> musicList.get(idx).getTitle().equals(activeAudio.getTitle())).findFirst().getAsInt();
                    }

                    preferencesUtil.storeAudioIndex(audioIndex);
                    preferencesUtil.storeShuffleState(true);
                }
                break;
        }

        handleActions(intent);
        return START_NOT_STICKY;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.e(getPackageName(), "onTrimMemory():" + level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(getPackageName(), "onLowMemory()");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        if (mediaPlayer == null) {
            stopSelf();
        }

        Log.e(getPackageName(), "Removing task");
    }

    private void initMedia() {
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);

        mediaPlayer.reset();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        );



        try {
            if (activeAudio == null) return;
            mediaPlayer.setDataSource(activeAudio.getPath());
            preferencesUtil.storeAudioTrackData(activeAudio.getPath());
        } catch (IOException e) {
            stopSelf();
            e.printStackTrace();
        }

        try {
            mediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void PlayCommand() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            buildNotification(ApplicationUtil.Status.PLAYING);
            updateMediaProgress(PlaybackState.STATE_PLAYING);

            emitActionToUI(mediaPlayer.isPlaying());

            LocalBroadcastManager
                    .getInstance(this)
                    .sendBroadcastSync(new Intent(PREPARE_CMD + ".running"));
        }
    }

    private void PauseCommand() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();

            buildNotification(ApplicationUtil.Status.PAUSED);
            updateMediaProgress(PlaybackState.STATE_PAUSED);
            emitActionToUI(mediaPlayer.isPlaying());
        }
    }

    private void StopCommand() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void SkipCommand() {

        if (mediaPlayer.isLooping()) {
            mediaPlayer.setLooping(false);
            preferencesUtil.storeLoopState(mediaPlayer.isLooping());
        }

        if (audioIndex == musicList.size() - 1) {
            audioIndex = 0;
            activeAudio = musicList.get(audioIndex);
            MEDIA_ACTIVE_TITLE = musicList.get(audioIndex).getTitle();
        } else {
            activeAudio = musicList.get(++audioIndex);
        }

        preferencesUtil.storeAudioIndex(audioIndex);
        preferencesUtil.storeAudioTitle(MEDIA_ACTIVE_TITLE);

        StopCommand();
        mediaPlayer.reset();
        initMedia();

        updateMetaData();
        buildNotification(ApplicationUtil.Status.PLAYING);
        updateMediaProgress(PlaybackState.STATE_SKIPPING_TO_NEXT);

        emitActionToUI(mediaPlayer.isPlaying());
    }

    private void PreviousCommand() {

        if (mediaPlayer.isLooping()) {
            mediaPlayer.setLooping(false);
            preferencesUtil.storeLoopState(mediaPlayer.isLooping());
        }


        if (audioIndex == 0) {
            audioIndex = musicList.size() - 1;
            activeAudio = musicList.get(audioIndex);
            MEDIA_ACTIVE_TITLE = musicList.get(audioIndex).getTitle();
        } else {
            activeAudio = musicList.get(--audioIndex);
        }

        preferencesUtil.storeAudioIndex(audioIndex);
        preferencesUtil.storeAudioTitle(MEDIA_ACTIVE_TITLE);

        StopCommand();
        mediaPlayer.reset();
        initMedia();

        updateMetaData();
        buildNotification(ApplicationUtil.Status.PLAYING);
        updateMediaProgress(PlaybackState.STATE_SKIPPING_TO_PREVIOUS);

        emitActionToUI(mediaPlayer.isPlaying());
    }

    private void ResumeCommand() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();

            buildNotification(ApplicationUtil.Status.PLAYING);
            updateMediaProgress(PlaybackState.STATE_PLAYING);

            emitActionToUI(mediaPlayer.isPlaying());
        }
    }

    private void AddFavoriteCommand(String music_name) {
        if (mediaPlayer == null) return;


        UserFavoritesHelper userFavoritesHelper = new UserFavoritesHelper(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int pos = IntStream.range(0, musicList.size())
                    .filter(itemPos -> musicList.get(itemPos).getTitle().equals(music_name)).findFirst().getAsInt();

            userFavoritesHelper.storeFavorite(musicList.get(pos).getTitle());
        }


    }

    private void RemoveFavoriteCommand(String music_name) {
        if (mediaPlayer == null) return;

        UserFavoritesHelper userFavoritesHelper = new UserFavoritesHelper(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int pos = IntStream.range(0, musicList.size())
                    .filter(itemPos -> musicList.get(itemPos).getTitle().equals(music_name)).findFirst().getAsInt();

            userFavoritesHelper.removeFavorite(musicList.get(pos).getTitle());
        }


    }

    private void updateMediaProgress(int state) {
        if (mediaPlayer == null) return;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer == null) return;
                int current = mediaPlayer.getCurrentPosition();
                playbackState =
                        new PlaybackState.Builder()
                                .setState(state, current, 1)
                                .setActions(PlaybackState.ACTION_SEEK_TO
                                        | PlaybackState.ACTION_PAUSE |
                                        PlaybackState.ACTION_PLAY |
                                        PlaybackState.ACTION_SKIP_TO_NEXT |
                                        PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                                        PlaybackState.ACTION_PREPARE
                                )
                                .build();
                mediaSession.setPlaybackState(playbackState);
            }
        }, 50);
    }

    private void initMediaSession() throws RemoteException {
        if (mediaSession != null) return;

        mediaSessionManager =
                (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);

        mediaSession = new MediaSession(getApplicationContext(), "Scutfy Music");
        transportControls = mediaSession.getController().getTransportControls();

        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);

        updateMetaData();

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                ResumeCommand();

                updateMediaProgress(PlaybackState.STATE_PLAYING);

                preferencesUtil.storePlayingState(true);
            }

            @Override
            public void onPause() {
                super.onPause();
                PauseCommand();

                updateMediaProgress(PlaybackState.STATE_PAUSED);

                preferencesUtil.storePlayingState(false);
            }

            @Override
            public void onStop() {
                super.onStop();
                StopCommand();
                preferencesUtil.storePlayingState(false);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                SkipCommand();

                updateMediaProgress(PlaybackState.STATE_SKIPPING_TO_NEXT);

                preferencesUtil.storePlayingState(true);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                PreviousCommand();

                updateMediaProgress(PlaybackState.STATE_SKIPPING_TO_PREVIOUS);

                preferencesUtil.storePlayingState(true);
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);

                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo((int) pos);
                    resumePosition = (int) pos;
                } else {
                    mediaPlayer.seekTo((int) pos);
                    updateMediaProgress(PlaybackState.STATE_PLAYING);
                }
            }
        });


        playbackState =
                new PlaybackState.Builder()
                        .setState(PlaybackState.STATE_PAUSED, 0, 1)
                        .setActions(PlaybackState.ACTION_SEEK_TO
                                | PlaybackState.ACTION_PAUSE |
                                PlaybackState.ACTION_PLAY |
                                PlaybackState.ACTION_SKIP_TO_NEXT |
                                PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackState.ACTION_PREPARE
                        )
                        .build();

        mediaSession.setPlaybackState(playbackState);
    }

    private void updateMetaData() {
        if (activeAudio == null) return;

        Uri url = Uri.parse(activeAudio.getAlbum(getBaseContext()));

        Bitmap albumArt = null;
        try {
            albumArt = MediaStore.Images.Media.getBitmap(getContentResolver(), url);
        } catch (IOException e) {
            albumArt = BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.img_default_music
            );
        }

        mediaSession.setMetadata(new android.media.MediaMetadata.Builder()
                .putBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(android.media.MediaMetadata.METADATA_KEY_ARTIST, activeAudio.getAuthor())
                .putString(android.media.MediaMetadata.METADATA_KEY_ALBUM, activeAudio.getTitle())
                .putString(android.media.MediaMetadata.METADATA_KEY_TITLE, activeAudio.getTitle())
                .putString(android.media.MediaMetadata.METADATA_KEY_MEDIA_URI, activeAudio.getPath())
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, activeAudio.getMime_Type())
                .putLong(android.media.MediaMetadata.METADATA_KEY_DURATION, convertString(activeAudio.getDuration()))
                .build());
    }

    private void buildNotification(ApplicationUtil.Status status) {
        int notificationAction = R.drawable.nf_pause;
        int notificationFavAction = R.drawable.btn_favorite;


        PendingIntent playAction_PauseAction = null;

        if (status == ApplicationUtil.Status.PLAYING) {
            notificationAction = R.drawable.nf_pause;
            playAction_PauseAction = playbackAction(1);
        } else if (status == ApplicationUtil.Status.PAUSED) {
            notificationAction = R.drawable.nf_play;
            playAction_PauseAction = playbackAction(0);
        }

        PendingIntent fav_unfavAction = null;


        if (isFavorite(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE))) {
            notificationFavAction = R.drawable.btn_favorite_active;
            fav_unfavAction = playbackAction(6);
        } else {
            notificationFavAction = R.drawable.btn_favorite;
            fav_unfavAction = playbackAction(5);
        }

        Bitmap cover = MediaMetadataUtil.getCover(getBaseContext(), audioIndex, musicList);

        Notification.Builder mBuilder = null;

        Intent mainIntent = new Intent(getBaseContext(), MusicActivity.class);
        PendingIntent mainPending = PendingIntent.getActivity(getBaseContext(), 1006, mainIntent, PendingIntent.FLAG_IMMUTABLE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder = new Notification.Builder(getBaseContext(), "Music Player")
                    .setShowWhen(true)
                    .setContentIntent(mainPending)
                    .setOnlyAlertOnce(true)
                    .setStyle(new Notification.MediaStyle()
                            .setShowActionsInCompactView(1, 2, 3)
                            .setMediaSession(mediaSession.getSessionToken())
                    )
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setLargeIcon(cover)
                    .setSmallIcon(R.drawable.img_splash)
                    .setContentText(activeAudio.getAuthor())
                    .setContentTitle(activeAudio.getTitle())
                    .setContentInfo(activeAudio.getTitle())
                    .addAction(notificationFavAction, "favorite", fav_unfavAction)
                    .addAction(R.drawable.nf_prev, "previous", playbackAction(3))
                    .addAction(notificationAction, "pause", playAction_PauseAction)
                    .addAction(R.drawable.nf_next, "next", playbackAction(2))
                    .addAction(R.drawable.nf_close, "close", playbackAction(4));

            startForeground(145, mBuilder.build());
        } else {

            MediaSession.Token token = mediaSession.getSessionToken();

            androidx.core.app.NotificationCompat.Builder notificationCompat
                    = new androidx.core.app.NotificationCompat.Builder(getBaseContext(),
                    "Music Player"
            )
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setMediaSession(MediaSessionCompat.Token.fromToken(token))
                            .setShowActionsInCompactView(0, 1, 2)
                    )
                    .setContentIntent(mainPending)
                    .setShowWhen(true)
                    .setOnlyAlertOnce(true)
                    .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                    .setLargeIcon(cover)
                    .setSmallIcon(R.drawable.img_splash)
                    .setContentText(activeAudio.getAuthor())
                    .setContentTitle(activeAudio.getTitle())
                    .setContentInfo(activeAudio.getTitle())
                    .addAction(notificationFavAction, "favorite", fav_unfavAction)
                    .addAction(R.drawable.nf_prev, "previous", playbackAction(3))
                    .addAction(notificationAction, "pause", playAction_PauseAction)
                    .addAction(R.drawable.nf_next, "next", playbackAction(2))
                    .addAction(R.drawable.nf_close, "close", playbackAction(4));
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getBaseContext());

            startForeground(145, notificationCompat.build());
        }
    }

    private PendingIntent playbackAction(int actionNumber) {
        Context context = getApplicationContext();
        Intent playbackAction = new Intent(context, MusicService.class);

        // FLAG_IMMUTABLE API 32&31(12l, 12) NÃO PODE SER MODIFICADO POR OUTRO APLICATIVO
        switch (actionNumber) {
            case 0:
                playbackAction.setAction(BroadcastConstants.ACTION_PLAY);
                return PendingIntent.getService(context, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 1:
                playbackAction.setAction(BroadcastConstants.ACTION_PAUSE);
                return PendingIntent.getService(context, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 2:
                playbackAction.setAction(BroadcastConstants.ACTION_SKIP);
                return PendingIntent.getService(context, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 3:
                playbackAction.setAction(BroadcastConstants.ACTION_PREV);
                return PendingIntent.getService(context, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 4:
                playbackAction.setAction(BroadcastConstants.ACTION_CLOSE);
                return PendingIntent.getService(context, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 5:
                playbackAction.setAction(BroadcastConstants.ACTION_FAVORITE);
                return PendingIntent.getService(context, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            case 6:
                playbackAction.setAction(BroadcastConstants.ACTION_FAVORITE_UNDO);
                return PendingIntent.getService(context, actionNumber, playbackAction, PendingIntent.FLAG_IMMUTABLE);
            default:
                break;
        }
        return null;
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager)
                getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void removeAudioFocus() {
        audioManager.abandonAudioFocus(this);
    }

    private void phoneCallListener() {
        telephonyManager = (TelephonyManager)
                getSystemService(TELEPHONY_SERVICE);

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            PauseCommand();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                ResumeCommand();
                            }
                        }
                        break;
                }
            }
        };

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void registerNoisyState() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }


    private void emitActionToUI(boolean isPlaying) {
        LocalBroadcastManager
                .getInstance(this)
                .sendBroadcastSync(new Intent(UI_UPDATE_MEDIA_CONTROL_BUTTON)
                        .putExtra("playback.status", isPlaying)
                );
    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                /*
                 * Resumir a reprodução
                 */
                if (mediaPlayer == null) initMedia();
                else if (!mediaPlayer.isPlaying()) {
                    ResumeCommand();
                }

                mediaPlayer.setVolume(1.0f, 1.0f);
                buildNotification(ApplicationUtil.Status.PLAYING);
                preferencesUtil.storePlayingState(true);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                /*
                 * Apps que reproduzem por um longo tempo..
                 * Quando o usuario mudar de app.
                 * Permitir que ele volte a reproduzir.
                 */
                if (mediaPlayer.isPlaying()) PauseCommand();

                preferencesUtil.storePlayingState(false);
                buildNotification(ApplicationUtil.Status.PAUSED);
                emitActionToUI(mediaPlayer.isPlaying());
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                /*
                 * Parar por um curto tempo (parar quando um status/stories com musica começar a tocar
                 * e Resumir quando o status/stories encerrar/fechar)
                 */
                if (mediaPlayer.isPlaying()) PauseCommand();
                buildNotification(ApplicationUtil.Status.PAUSED);
                preferencesUtil.storePlayingState(false);

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Quando receber uma notificação, diminua o volume
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.d(getPackageName(), "onBufferingUpdate():" + i);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(getPackageName(), "onCompletion()");
        if (musicList.size() == 0) return;

        SkipCommand();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + i1);
                Toast.makeText(getBaseContext(), "PROGRESSIVE_PLAYBACK", Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + i1);
                Toast.makeText(getBaseContext(), "SERVER_DIED", Toast.LENGTH_SHORT).show();

                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + i1);
                Toast.makeText(getBaseContext(), "ERROR UNKNOWN", Toast.LENGTH_SHORT).show();
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
        if (resume) {

            LocalBroadcastManager
                    .getInstance(this)
                    .sendBroadcastSync(new Intent(PREPARE_CMD + ".running")

                    );


            resumePosition = preferencesUtil.loadLastPosition();
            mediaPlayer.seekTo(preferencesUtil.loadLastPosition());

            PauseCommand();
            buildNotification(ApplicationUtil.Status.PAUSED);
            updateMediaProgress(PlaybackState.STATE_PAUSED);
            emitActionToUI(mediaPlayer.isPlaying());

            preferencesUtil.storePlayingState(false);
            resume = false;
        } else {
            PlayCommand();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    private void handleActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_SKIP)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREV)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        } else if (actionString.equalsIgnoreCase(ACTION_CLOSE)) {
            removeAudioFocus();
            PauseCommand();
            buildNotification(ApplicationUtil.Status.PAUSED);
            preferencesUtil.storePlayingState(false);
            stopForeground(true);
        } else if (actionString.equalsIgnoreCase(ACTION_FAVORITE)) {
            if (isFavorite(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE))) {
                RemoveFavoriteCommand(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE));
            } else {
                AddFavoriteCommand(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE));
            }

            if (mediaPlayer.isPlaying()) {
                buildNotification(ApplicationUtil.Status.PLAYING);
            } else {
                buildNotification(ApplicationUtil.Status.PAUSED);
            }

            emitActionToUI(mediaPlayer.isPlaying());
        } else if (actionString.equalsIgnoreCase(ACTION_FAVORITE_UNDO)) {
            if (!isFavorite(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE))) {
                AddFavoriteCommand(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE));
            } else {
                RemoveFavoriteCommand(getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE));
            }

            if (mediaPlayer.isPlaying()) {
                buildNotification(ApplicationUtil.Status.PLAYING);
            } else {
                buildNotification(ApplicationUtil.Status.PAUSED);
            }

            emitActionToUI(mediaPlayer.isPlaying());
        }
    }


    private boolean isFavorite(String MUSIC_TITLE) {

        UserFavoritesHelper userFavoritesHelper = new UserFavoritesHelper(this);

        if (mediaPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                int pos = IntStream.range(0, musicList.size())
                        .filter(itemPos -> musicList.get(itemPos).getTitle().equals(MUSIC_TITLE)).findFirst().getAsInt();

                return userFavoritesHelper.isFavorite(musicList.get(pos).getTitle());
            }
        }

        return false;
    }

    private boolean isShuffle() {
        return preferencesUtil.loadShuffleState();
    }

    private boolean isLooping() {
        return mediaPlayer.isLooping();
    }


    public TransportControls getTransportControls() {
        return transportControls;
    }
    public PlaybackState getPlaybackState() {
        return playbackState;
    }

    public android.media.MediaMetadata getMetadata() {
        return mediaSession.getController().getMetadata();
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) return mediaPlayer.getCurrentPosition();
        return 0;
    }

    public ArrayList<UserPlaylist> getMusicList() {
        return musicList;
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    private static native int convertString(String text);
    static {
        System.loadLibrary("scutfy-msp-c");
    }
}