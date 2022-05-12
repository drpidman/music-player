package com.drkryz.scutfy.Screens;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.drkryz.scutfy.Constants.BroadcastConstants;
import com.drkryz.scutfy.Listeners.ItemClickSupport;
import com.drkryz.scutfy.Listeners.OnSwipeTouchListener;
import com.drkryz.scutfy.R;
import com.drkryz.scutfy.Screens.adapters.MusicRecyclerView;
import com.drkryz.scutfy.Services.MusicService;
import com.drkryz.scutfy.Utils.ContentManagerUtil;
import com.drkryz.scutfy.Utils.PreferencesUtil;
import com.drkryz.scutfy.Utils.ServiceManagerUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class MusicActivity extends AppCompatActivity {


    static {
        System.loadLibrary("scutfy-msp-c");
    }

    private MusicService musicService;
    private PreferencesUtil preferencesUtil;
    private LinearLayoutManager layoutManager;
    private View mediaBottomControl;
    private RecyclerView listView;
    private ImageView coverImage;
    private TextView musicTitle, musicAuthor;
    private ImageButton mediaControlPlayButton;
    private Toolbar toolbar;

    private ArrayList<UserPlaylist> musics;


    private boolean serviceBound = false;
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

    private boolean isRunning = false;
    public BroadcastReceiver MusicServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            isRunning = true;
        }
    };

    private boolean isPlaying = false;
    private final BroadcastReceiver PlaybackStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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


        listView = findViewById(R.id.musicList);
        coverImage = findViewById(R.id.mediaAlbumArt);
        musicTitle = findViewById(R.id.mediaTitle);
        musicAuthor = findViewById(R.id.mediaArtist);
        mediaControlPlayButton = findViewById(R.id.mediaControlPlayButton);
        mediaBottomControl = findViewById(R.id.mediaBottomControl);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayShowTitleEnabled(false);


        if (getIntent().getExtras() == null) {
            musicList =
                    new ContentManagerUtil(this).getMusics(this);

            musics = new ArrayList<>();
            musics.addAll(musicList);

        } else musicList = new Gson().fromJson(
                getIntent().getExtras().getString("com.drkryz.array.musics"),
                new TypeToken<ArrayList<UserPlaylist>>() {
                }.getType());


        // list view adapter

        adapter = new MusicRecyclerView(musicList, getBaseContext(), musicService);

        createChannel();
        preferencesUtil = new PreferencesUtil(getBaseContext());



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
                .setOnItemClickListener((recyclerView, position, v) -> {
                    UserPlaylist userPlaylist = musicList.get(position);

                    ContentManagerUtil contentManagerUtil = new ContentManagerUtil(getBaseContext());

                    preferencesUtil.storeAudioIndex(contentManagerUtil.getMusicIndexByName(userPlaylist.getTitle(), getBaseContext()));

                    Play();

                    if (!preferencesUtil.getFirstInit()) {
                        preferencesUtil.setInitFirst(true);

                        mediaBottomControl.setVisibility(View.VISIBLE);
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



        mediaBottomControl.setOnClickListener(view -> {
            Intent PlayerActivityIntent = new Intent(getBaseContext(), PlayerActivity.class);
            startActivity(PlayerActivityIntent);
        });

        // start to first
        ServiceManagerUtil.handleAction(0, this);
        bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);


        if (!preferencesUtil.getFirstInit()) {
            mediaBottomControl.setVisibility(View.INVISIBLE);
        }

        mediaControlPlayButton.setOnClickListener(view -> {
            int audioIndex = preferencesUtil.loadAudioIndex();

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
        });


        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    toolbar.setVisibility(View.VISIBLE);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    AnimationDrawable animationDrawable;

    private void updateCoverImage() throws IOException {
        if (musicService == null) return;

        MediaMetadata metadata = musicService.getMetadata();
        Bitmap cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

        coverImage.setImageBitmap(cover);
        musicTitle.setText(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        musicAuthor.setText(metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));

        musicTitle.setSelected(true);

        if (isPlaying) {
            mediaControlPlayButton.setBackgroundResource(R.drawable.sc_mn_anim_playback_play);
        } else {
            mediaControlPlayButton.setBackgroundResource(R.drawable.sc_mn_anim_playback_pause);
        }


        animationDrawable = (AnimationDrawable) mediaControlPlayButton.getBackground();
        animationDrawable.start();


    }

    private void Play() {

        if (!isRunning) {
            Intent service = new Intent(this, MusicService.class);
            service.setAction(BroadcastConstants.INIT_CMD);

            bindService(service, serviceConnection, BIND_AUTO_CREATE);
            ServiceManagerUtil.handleAction(1, this);
        } else {
            ServiceManagerUtil.handleAction(2, this);
        }
    }

    @SuppressLint({"ResourceType", "UseCompatLoadingForDrawables"})
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);


        androidx.appcompat.widget.SearchView searchView  = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setBackgroundResource(R.drawable.obj_searchview_background);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(getPackageName(), "onStart()");

        if (preferencesUtil.loadAudioIndex() != -1) {
            if (!isRunning) {
                ServiceManagerUtil.handleAction(7, this);
                ServiceManagerUtil.handleAction(11, this);
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
        new Handler().postDelayed(() -> {
            if (isRunning) {
                isPlaying = preferencesUtil.getPlayingState();

                ServiceManagerUtil.handleAction(11, getBaseContext());
                try {
                    updateCoverImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                layoutManager.scrollToPosition(preferencesUtil.loadAudioIndex());
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
        NotificationChannel channel;
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
                    new NotificationChannelCompat.Builder(
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
}