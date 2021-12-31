package com.drkryz.musicplayer.Screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.drkryz.musicplayer.Class.Default.UserPlaylist;
import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.Utils.ContentManagerUtil;
import com.drkryz.musicplayer.Utils.ServiceManagerUtil;
import com.drkryz.musicplayer.Listeners.ItemClickSupport;
import com.drkryz.musicplayer.Listeners.OnSwipeTouchListener;
import com.drkryz.musicplayer.Screens.adapters.MusicRecyclerView;
import com.drkryz.musicplayer.Services.MusicService;
import com.drkryz.musicplayer.Constants.BroadcastConstants;
import com.drkryz.musicplayer.Utils.ApplicationUtil;
import com.drkryz.musicplayer.Utils.PreferencesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class MusicActivity extends AppCompatActivity {


    private MusicService musicService;
    private PreferencesUtil preferencesUtil;
    private LinearLayoutManager layoutManager;


    private View mediaBottomControl;

    private ImageView coverImage;
    private TextView musicTitle, musicAuthor;
    private ImageButton mediaControlPlayButton;

    private boolean serviceBound = false;
    private boolean isRunning = false;
    private boolean isPlaying = false;

    private ArrayList<UserPlaylist> musicList;
    private MusicRecyclerView adapter;


    @SuppressLint({"ServiceCast", "ClickableViewAccessibility", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(getBaseContext().getPackageName(), "onCreate()");


        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(PlaybackStatusReceiver, new IntentFilter(BroadcastConstants.UI_UPDATE_MEDIA_CONTROL_BUTTON));

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(MusicServiceStatus, new IntentFilter(BroadcastConstants.PREPARE_CMD + ".running"));


        RecyclerView listView = (RecyclerView) findViewById(R.id.musicList);
        coverImage = (ImageView) findViewById(R.id.mediaAlbumArt);
        musicTitle = (TextView) findViewById(R.id.mediaTitle);
        musicAuthor = (TextView) findViewById(R.id.mediaArtist);
        mediaControlPlayButton = (ImageButton) findViewById(R.id.mediaControlPlayButton);
        mediaBottomControl = (View) findViewById(R.id.mediaBottomControl);

        if (getIntent().getExtras() == null) {
            musicList =
                    new ContentManagerUtil(this).getMusics();
        } else musicList = new Gson().fromJson(
                getIntent().getExtras().getString("com.drkryz.array.musics"),
                new TypeToken<ArrayList<UserPlaylist>>() {}.getType());


        // list view adapter

        adapter = new MusicRecyclerView(musicList, getBaseContext(), musicService);


        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);

        alphaInAnimationAdapter.setDuration(200);
        alphaInAnimationAdapter.setInterpolator(new AnticipateInterpolator());
        alphaInAnimationAdapter.setFirstOnly(false);

        listView.setAdapter(new SlideInBottomAnimationAdapter(alphaInAnimationAdapter));


        layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setHasFixedSize(true);

        // list view click listener
        ItemClickSupport.addTo(listView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Play(position);
                    }
                });


        mediaBottomControl.setOnTouchListener(new OnSwipeTouchListener() {
            @Override
            public boolean onSwipeTop() {
                Intent PlayerActivityIntent = new Intent(getBaseContext(), PlayerActivity.class);
                startActivity(PlayerActivityIntent);
                return super.onSwipeTop();
            }
        });


        createChannel();
        preferencesUtil = new PreferencesUtil(getBaseContext());
        preferencesUtil.storeUserInApp(true);
        preferencesUtil.storeAudio(musicList);

        mediaBottomControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent PlayerActivityIntent = new Intent(getBaseContext(), PlayerActivity.class);
                startActivity(PlayerActivityIntent);
            }
        });

        // start to first
        ServiceManagerUtil.handleAction(0, this);
        bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);


        mediaControlPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int audioIndex = preferencesUtil.loadAudioIndex();

                Log.e(getPackageName(), "running=" + isRunning);

                if (isRunning) {
                    if (isPlaying) {
                        ServiceManagerUtil.handleAction(3, getBaseContext());
                    } else {
                        ServiceManagerUtil.handleAction(4, getBaseContext());
                    }
                } else {
                    if (audioIndex != -1) {
                        Intent service = new Intent(getBaseContext(), MusicService.class);
                        service.setAction(BroadcastConstants.INIT_CMD);

                        bindService(service, serviceConnection, BIND_AUTO_CREATE);
                        startService(service);
                    }
                }
            }
        });
    }


    private final BroadcastReceiver PlaybackStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(getPackageName(), "playback.status:" + "received");
            isPlaying = intent.getBooleanExtra("playback.status", false);

            if (isPlaying) {
                try {
                    updateCoverImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    updateCoverImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    public BroadcastReceiver MusicServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            isRunning = true;
        }
    };

    private void updateCoverImage() throws IOException {
        if (musicService == null) return;

        MediaMetadata metadata = musicService.getMetadata();
        Bitmap cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cover.compress(Bitmap.CompressFormat.JPEG, 100, out);
        Bitmap decode = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        coverImage.setImageBitmap(decode);
        musicTitle.setText(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        musicAuthor.setText(metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));

        if (isPlaying) {
            mediaControlPlayButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.nf_pause));
        } else {
            mediaControlPlayButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.nf_play));
        }


    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(getPackageName(), "service connected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) iBinder;

            musicService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(getPackageName(), "service disconnected");
            serviceBound = false;
        }
    };

    private void Play(int position) {

        if (!isRunning) {
            preferencesUtil.storeAudioIndex(position);
            Intent service = new Intent(this, MusicService.class);
            service.setAction(BroadcastConstants.INIT_CMD);

            bindService(service, serviceConnection, BIND_AUTO_CREATE);
            ServiceManagerUtil.handleAction(1, this);
        } else {
            preferencesUtil.storeAudioIndex(position);
            ServiceManagerUtil.handleAction(2, this);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e(getPackageName(), "onStart()");

        if (preferencesUtil.loadAudioIndex() != -1) {
            if (!isRunning) {
                ServiceManagerUtil.handleAction(7, this);
            }
        }

        preferencesUtil.storeUserInApp(true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(getPackageName(), "onPause()");


        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(MusicServiceStatus);
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(PlaybackStatusReceiver);

        preferencesUtil.storeUserInApp(false);
        preferencesUtil.storePlayingState(isPlaying);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume()", "PlayerActivity():serviceBound=" + serviceBound);


        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(PlaybackStatusReceiver, new IntentFilter(BroadcastConstants.UI_UPDATE_MEDIA_CONTROL_BUTTON));

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(MusicServiceStatus, new IntentFilter(BroadcastConstants.PREPARE_CMD + ".running"));

        preferencesUtil.storeUserInApp(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    isPlaying = preferencesUtil.getPlayingState();

                    try {
                        updateCoverImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    layoutManager.scrollToPosition(preferencesUtil.loadAudioIndex());
                }
            }
        }, 16);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(MusicServiceStatus);
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(PlaybackStatusReceiver);

        preferencesUtil.storeUserInApp(false);

        unbindService(serviceConnection);
        Log.e(getPackageName(), "activity destroyed");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.e("savingInstance", "saved");
        outState.putBoolean("serviceStatus", serviceBound);
        outState.putBoolean("isPlaying", isPlaying);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.e("onRestoreInstance", "called");

        serviceBound = savedInstanceState.getBoolean("serviceStatus");
        isPlaying = savedInstanceState.getBoolean("isPlaying");

        super.onRestoreInstanceState(savedInstanceState);
    }


    private void createChannel() {
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    "Music Player",
                    "Music Player",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription("Notificação do controle de mídia");
            channel.enableVibration(false);


            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        } else {
            NotificationChannelCompat.Builder notificationChannel =
                    (NotificationChannelCompat.Builder) new NotificationChannelCompat.Builder(
                            "Music Player",
                            NotificationManagerCompat.IMPORTANCE_DEFAULT
                    );

            notificationChannel.setDescription("Notificação do controle de mídia");
            notificationChannel.setVibrationEnabled(false);

            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(getApplicationContext());

            notificationManager.createNotificationChannel(notificationChannel.build());
        }
    }

    static {
        System.loadLibrary("musicplayer");
    }
}