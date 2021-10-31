package com.drkryz.musicplayer.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
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
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.ExternalStorage;
import com.drkryz.musicplayer.functions.ServiceManager;
import com.drkryz.musicplayer.listeners.ItemClickSupport;
import com.drkryz.musicplayer.listeners.MainListeners.TransitionListener;
import com.drkryz.musicplayer.listeners.OnSwipeTouchListener;
import com.drkryz.musicplayer.screens.adapters.MusicRecyclerView;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.ApplicationUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;

public class MusicActivity extends AppCompatActivity {


    private ApplicationUtil applicationUtil;
    private MusicService musicService;
    private ExternalStorage externalGet;
    private PreferencesUtil preferencesUtil;
    private LinearLayoutManager layoutManager;
    private RecyclerView listView;


    private View mediaBottomControl;

    private ImageView coverImage;
    private TextView musicTitle, musicAuthor;
    private ImageButton mediaControlPlayButton;

    private boolean serviceBound = false;
    private boolean isRunning = false;
    private boolean isPlaying = false;



    @SuppressLint({"ServiceCast", "ClickableViewAccessibility"})
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



        listView = (RecyclerView) findViewById(R.id.musicList);
        coverImage = (ImageView) findViewById(R.id.mediaAlbumArt);
        musicTitle = (TextView) findViewById(R.id.mediaTitle);
        musicAuthor = (TextView) findViewById(R.id.mediaArtist);
        mediaControlPlayButton = (ImageButton) findViewById(R.id.mediaControlPlayButton);

        mediaBottomControl = (View) findViewById(R.id.mediaBottomControl);

        applicationUtil = (ApplicationUtil) getApplicationContext();
        externalGet = new ExternalStorage();
        externalGet.populateSongs(getApplication());


        // list view adapter
        MusicRecyclerView adapter = new MusicRecyclerView(externalGet.getAll(), getBaseContext());
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);

        alphaInAnimationAdapter.setDuration(1000);
        alphaInAnimationAdapter.setInterpolator(new AccelerateInterpolator());
        alphaInAnimationAdapter.setFirstOnly(false);

        listView.setAdapter(new ScaleInAnimationAdapter(alphaInAnimationAdapter));

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
        preferencesUtil.StoreUserInApp(true);
        preferencesUtil.storeAudio(externalGet.getAll());

        mediaBottomControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent PlayerActivityIntent = new Intent(getBaseContext(), PlayerActivity.class);
                startActivity(PlayerActivityIntent);
            }
        });

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (preferencesUtil.loadAudio().size() < 1) return;

                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();

                boolean endHasBeenReached = lastVisible + 1 >= totalItemCount;

                if (totalItemCount > 0 && endHasBeenReached) {
                    mediaBottomControl.setVisibility(View.INVISIBLE);
                } else {
                    mediaBottomControl.setVisibility(View.VISIBLE);
                }
            }
        });


        // start to first
        ServiceManager.handleAction(0, this);
        bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);


        mediaControlPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int audioIndex = preferencesUtil.loadAudioIndex();

                Log.e(getPackageName(), "running=" + isRunning);

                if (isRunning) {
                    if (isPlaying) {
                        ServiceManager.handleAction(3, getBaseContext());
                    } else {
                        ServiceManager.handleAction(4, getBaseContext());
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
        if (!preferencesUtil.LoadUserInApp()) return;

        MediaMetadata metadata = musicService.getMetadata();
        Bitmap cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cover.compress(Bitmap.CompressFormat.JPEG, 100, out);
        Bitmap decode = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        coverImage.setImageBitmap(decode);
        musicTitle.setText(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        musicAuthor.setText(metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));

        if (isPlaying) {
            mediaControlPlayButton.setImageDrawable(getDrawable(R.drawable.nf_pause));
        } else {
            mediaControlPlayButton.setImageDrawable(getDrawable(R.drawable.nf_play));
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
            ServiceManager.handleAction(1, this);
        } else {
            preferencesUtil.storeAudioIndex(position);
            ServiceManager.handleAction(2, this);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e(getPackageName(), "onStart()");
        Log.e(getPackageName(), "" + preferencesUtil.GetFirstInit());

        if (preferencesUtil.loadAudioIndex() != -1) {
            if (!isRunning) {
                ServiceManager.handleAction(7, this);
            }
        }

        preferencesUtil.StoreUserInApp(true);
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

        preferencesUtil.StoreUserInApp(false);
        preferencesUtil.StorePlayingState(isPlaying);
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

        preferencesUtil.StoreUserInApp(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    isPlaying = preferencesUtil.GetPlayingState();

                    try {
                        updateCoverImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    layoutManager.scrollToPosition(preferencesUtil.loadAudioIndex());
                }
            }
        }, 250);
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

        preferencesUtil.StoreUserInApp(false);

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