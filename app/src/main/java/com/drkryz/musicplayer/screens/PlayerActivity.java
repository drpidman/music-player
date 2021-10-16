package com.drkryz.musicplayer.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.GetMusicsFromExt;
import com.drkryz.musicplayer.functions.PlaybackAlbum;
import com.drkryz.musicplayer.listeners.ItemClickSupport;
import com.drkryz.musicplayer.listeners.MainListeners.TransitionListener;
import com.drkryz.musicplayer.listeners.OnSwipeTouchListener;
import com.drkryz.musicplayer.screens.adapters.MusicRecyclerView;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.ApplicationUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class PlayerActivity extends AppCompatActivity {


    private ApplicationUtil applicationUtil;
    private MusicService musicService;
    private BroadcastUtils broadcastUtils;
    private GetMusicsFromExt externalGet;
    private PreferencesUtil preferencesUtil;

    private MotionLayout motionLayout;
    private RecyclerView listView;
    private ImageButton PlayUiBtn;
    private ImageView coverImage;
    private TextView currentPlayingText;
    private SeekBar seekBar;
    private View bottomNav;

    private boolean serviceBound = false;
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

        motionLayout = (MotionLayout) findViewById(R.id.MainMotion);
        motionLayout.setTransitionListener(new TransitionListener(this));


        ImageButton UiPrevious = (ImageButton) findViewById(R.id.uiPrevious);
        ImageButton UiSkip = (ImageButton) findViewById(R.id.uiSkip);
        currentPlayingText = (TextView) findViewById(R.id.currentSongTitle);
        listView = (RecyclerView) findViewById(R.id.musicList);
        bottomNav = (View) findViewById(R.id.bottomNavDrag);
        seekBar = (SeekBar) findViewById(R.id.progressBar);
        PlayUiBtn = (ImageButton) findViewById(R.id.uiPlay);
        coverImage = (ImageView) findViewById(R.id.musicAlbum);


        applicationUtil = (ApplicationUtil) getApplicationContext();
        externalGet = new GetMusicsFromExt();
        externalGet.populateSongs(getApplication());


        // list view adapter
        MusicRecyclerView adapter = new MusicRecyclerView(externalGet.getAll(), getBaseContext());
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(adapter);
        alphaInAnimationAdapter.setDuration(5000);
        alphaInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        alphaInAnimationAdapter.setFirstOnly(false);

        listView.setAdapter(new ScaleInAnimationAdapter(alphaInAnimationAdapter));
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setHasFixedSize(true);

        // list view click listener
        ItemClickSupport.addTo(listView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Play(position);

                    }
                });

        UiPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Previous();
            }
        });

        UiSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Skip();
            }
        });

        PlayUiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int audioIndex = preferencesUtil.loadAudioIndex();

                if (serviceBound) {
                    if (isPlaying) {
                        Pause();
                    } else {
                        Resume();
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

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        createChannel();
        broadcastUtils = new BroadcastUtils(getApplicationContext());
        preferencesUtil = new PreferencesUtil(getBaseContext());
        preferencesUtil.StoreUserInApp(true);

        preferencesUtil.storeAudio(externalGet.getAll());

        Intent service = new Intent(this, MusicService.class);
        service.setAction(BroadcastConstants.PREPARE_CMD);
        startService(service);
    }


    private BroadcastReceiver PlaybackStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(getPackageName(), "PlayerActivity():status received");

            isPlaying = intent.getBooleanExtra("playback.status", false);

            if (isPlaying) {
                PlayUiBtn
                        .setImageDrawable(getDrawable(R.drawable.ui_pause));
                try {
                    updateCoverImage();
                    startSeekBar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                PlayUiBtn
                        .setImageDrawable(getDrawable(R.drawable.ui_play));
            }
        }
    };

    public BroadcastReceiver MusicServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
        }
    };

    private void updateSeekBar() {
        MediaMetadata mediaMetadata = musicService.getMetadata();
        seekBar.setMax((int) mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION));
    }

    private final Handler mHandler = new Handler();

    private void startSeekBar() {
        if (!preferencesUtil.LoadUserInApp()) return;
        if (!isPlaying) return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService == null) return;
                seekBar.setProgress(musicService.getCurrentPosition());
                mHandler.postDelayed(this, 1000);
            }
        });
    }


    private void updateCoverImage() throws IOException {

        MediaMetadata metadata = musicService.getMetadata();
        Bitmap cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cover.compress(Bitmap.CompressFormat.JPEG, 100, out);
        Bitmap decode = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));

        if (!preferencesUtil.LoadUserInApp()) return;
        coverImage.setImageBitmap(decode);
        currentPlayingText.setSelected(true);
        currentPlayingText.setText(
                metadata.getText(MediaMetadata.METADATA_KEY_TITLE)
        );

        updateSeekBar();
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

        if (!serviceBound) {
            preferencesUtil.storeAudioIndex(position);
            Intent service = new Intent(this, MusicService.class);
            service.setAction(BroadcastConstants.INIT_CMD);

            bindService(service, serviceConnection, BIND_AUTO_CREATE);
            startService(service);
        } else {
            preferencesUtil.storeAudioIndex(position);
            Intent service = new Intent(this, MusicService.class);
            service.setAction(BroadcastConstants.PLAY_CMD);
            startService(service);
        }
    }

    private void Pause() {
        Intent service = new Intent(this, MusicService.class);
        service.setAction(BroadcastConstants.PAUSE_CMD);
        startService(service);
    }

    private void Resume() {
        Intent service = new Intent(this, MusicService.class);
        service.setAction(BroadcastConstants.RESUME_CMD);
        startService(service);
    }

    private void Skip() {
        Intent service = new Intent(this, MusicService.class);
        service.setAction(BroadcastConstants.SKIP_CMD);
        startService(service);
    };

    private void Previous() {
        Intent service = new Intent(this, MusicService.class);
        service.setAction(BroadcastConstants.PREV_CMD);
        startService(service);
    };
    // ===============================


    @Override
    protected void onStart() {
        super.onStart();
        Log.e(getPackageName(), "onStart()");
        Log.e(getPackageName(), "" + preferencesUtil.GetFirstInit());

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
                if (serviceBound) {
                    isPlaying = preferencesUtil.GetPlayingState();

                    if (isPlaying) {
                        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
                        try {
                            updateCoverImage();
                            startSeekBar();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_play));
                        try {
                            updateCoverImage();
                            startSeekBar();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
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
}