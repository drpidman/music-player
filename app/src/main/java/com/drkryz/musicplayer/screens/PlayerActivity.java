package com.drkryz.musicplayer.screens;

import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION;
import static android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE;

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
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.custom.CustomItemAnimator;
import com.drkryz.musicplayer.functions.GetMusicsFromExt;
import com.drkryz.musicplayer.functions.PlaybackAlbum;
import com.drkryz.musicplayer.listeners.ItemClickSupport;
import com.drkryz.musicplayer.listeners.MainListeners.TransitionListener;
import com.drkryz.musicplayer.listeners.OnSwipeTouchListener;
import com.drkryz.musicplayer.screens.adapters.MusicRecyclerView;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class PlayerActivity extends AppCompatActivity {


    private GlobalsUtil globalsUtil;
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


    @SuppressLint({"ServiceCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.e(getBaseContext().getPackageName(), "onCreate()");

        registerReceiver(receivePlaying,
                new BroadcastUtils(getBaseContext()).playbackUIFilter(BroadcastConstants.RequestPlayChange)
        );
        registerReceiver(updateCover,
                new BroadcastUtils(getBaseContext()).playbackUIFilter(BroadcastConstants.UpdateCover)
        );

        globalsUtil = (GlobalsUtil) getApplicationContext();
        externalGet = new GetMusicsFromExt();

        // loadMusics
        externalGet.populateSongs(getApplication());
        globalsUtil.setMusicList(externalGet.getAll());


        if (globalsUtil.musicService != null) {
            Log.e("not a null", "true");
        }


        motionLayout = (MotionLayout) findViewById(R.id.MainMotion);
        motionLayout.setTransitionListener(new TransitionListener(this));
        motionLayout.setOnTouchListener(new OnSwipeTouchListener() {
            @Override
            public boolean onSwipeLeft() {
                if (globalsUtil.isServiceBound()) {
                    Skip();
                }
                return super.onSwipeLeft();
            }

            @Override
            public boolean onSwipeRight() {
                if (globalsUtil.isServiceBound()) {
                    Previous();
                }
                return super.onSwipeRight();
            }
        });


        ImageButton UiPrevious = (ImageButton) findViewById(R.id.uiPrevious);
        ImageButton UiSkip = (ImageButton) findViewById(R.id.uiSkip);

        currentPlayingText = (TextView) findViewById(R.id.currentSongTitle);
        listView = (RecyclerView) findViewById(R.id.musicList);
        bottomNav = (View) findViewById(R.id.bottomNavDrag);
        seekBar = (SeekBar) findViewById(R.id.progressBar);
        PlayUiBtn = (ImageButton) findViewById(R.id.uiPlay);
        coverImage = (ImageView) findViewById(R.id.musicAlbum);

        // list view adapter

        MusicRecyclerView adapter = new MusicRecyclerView(globalsUtil.getMusicList(), getBaseContext());
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setHasFixedSize(true);
        listView.setItemAnimator(new CustomItemAnimator());

        // list view click listener
        ItemClickSupport.addTo(listView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Play(position);
                        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
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

                boolean state = false;

                if (globalsUtil.musicService == null) {
                    state = preferencesUtil.GetPlayingState();
                } else {
                    state = globalsUtil.musicService.getPlayingState();
                }



                Log.d("PLAYBACK", "" + state);
                if (state) {
                    Pause();
                    PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_play));
                    seekBar.setVisibility(View.INVISIBLE);
                } else {
                    Resume();
                    PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
                    seekBar.setVisibility(View.VISIBLE);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (globalsUtil.isServiceBound()) {
                    if (fromUser) globalsUtil.transportControls.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        broadcastUtils = new BroadcastUtils(getApplicationContext());
        preferencesUtil = new PreferencesUtil(getBaseContext());

        preferencesUtil.StoreUserInApp(true);

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


    private boolean serviceState() {
        return globalsUtil.isServiceBound();
    }

    private void updateSeekBar() {

        if (globalsUtil.musicService != null) {
            seekBar.setMax(globalsUtil.musicService.getTotalDuration());
        } else {
            seekBar.setMax(Integer.parseInt(preferencesUtil.LoadTotalDuration()));
        }
    }

    private Handler mHandler = new Handler();

    private void startSeekBar() {
        if (globalsUtil.isServiceBound()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!preferencesUtil.LoadUserInApp()) return;
                    seekBar.setProgress(globalsUtil.musicService.getCurrentPosition());
                    mHandler.postDelayed(this, 1000);
                }
            });
        }
    }


    private void updateCoverImage() throws IOException {
        if (!serviceState()) return;


        Bitmap cover = PlaybackAlbum.getCover(getBaseContext(), globalsUtil.audioIndex, preferencesUtil.loadAudio());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cover.compress(Bitmap.CompressFormat.JPEG, 100, out);
        Bitmap decode = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));


        if (!preferencesUtil.LoadUserInApp()) return;
        coverImage.setImageBitmap(decode);
        currentPlayingText.setSelected(true);
        currentPlayingText.setText(globalsUtil.activeAudio.getTitle());
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(getPackageName(), "service connected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) iBinder;
            globalsUtil.musicService = binder.getService();

            globalsUtil.setServiceBound(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(getPackageName(), "service disconnected");
            globalsUtil.setServiceBound(false);
        }
    };

    private void Play(int position) {
        if (!globalsUtil.isServiceBound()) {

            preferencesUtil.storeAudio(globalsUtil.getMusicList());
            preferencesUtil.storeAudioIndex(position);

            Intent player = new Intent(getBaseContext(), MusicService.class);

            startService(player);
            bindService(player, serviceConnection, BIND_AUTO_CREATE);
        } else {
            preferencesUtil.storeAudioIndex(position);
            broadcastUtils.playbackUIManager(BroadcastConstants.Play, false);
        }
    }

    // controls ===============
    private void Pause() {
        if (!serviceState()) return;
        globalsUtil.mediaSession.getController().getTransportControls().pause();
        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_play));
        seekBar.setVisibility(View.INVISIBLE);
    }

    private void Resume() {
        if (!serviceState()) return;
        globalsUtil.mediaSession.getController().getTransportControls().play();
        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
        seekBar.setVisibility(View.VISIBLE);
    }

    private void Skip() {
        if (!serviceState()) return;
        globalsUtil.mediaSession.getController().getTransportControls().skipToNext();
        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
        seekBar.setVisibility(View.VISIBLE);
    };

    private void Previous() {
        if (!serviceState()) return;
        globalsUtil.mediaSession.getController().getTransportControls().skipToPrevious();
        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
        seekBar.setVisibility(View.VISIBLE);
    };
    // ===============================

    private final BroadcastReceiver receivePlaying = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("playingState", false)) {
                PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
                seekBar.setVisibility(View.VISIBLE);
            } else {
                PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_play));
                seekBar.setVisibility(View.INVISIBLE);
            }
        }
    };

    private final BroadcastReceiver updateCover = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                updateCoverImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateSeekBar();
            startSeekBar();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(getPackageName(), "onStart()");
        Log.e(getPackageName(), "" + preferencesUtil.GetFirstInit());
    }


    @Override
    protected void onPause() {
        super.onPause();

        Log.e(getBaseContext().getPackageName(), "onPause()");
        if (globalsUtil.isServiceBound()) {
            if (globalsUtil.musicService.getPlayingState()) {
                preferencesUtil.SetLastIndex(globalsUtil.audioIndex);
                preferencesUtil.StoreCurrentTotalDuration(globalsUtil.activeAudio.getDuration());

                preferencesUtil.SetLastCover(
                        globalsUtil.activeAudio.getAlbum()
                );

                preferencesUtil.StoreUserInApp(false);

            }

            LocalBroadcastManager
                    .getInstance(this)
                    .sendBroadcastSync(new Intent("user.state.view")
                    .putExtra("user.state", 0)
                    );
        }

        if (globalsUtil.isServiceBound()) {
            bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);
        }

        if (preferencesUtil.GetFirstInit()) {
            Log.e(getPackageName(), "retomando a musica anterior");

            boolean playingState = preferencesUtil.GetPlayingState();
            bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            /**
             * RESUMIR ESTADO ANTERIOR DA VIEW.
             * SE APLICA QUANDO O USUARIO FECHAR O APLICATIVO PELO BOTÃO "voltar" fisico ou virtual.
             * SE APLICA QUANDO O USUARIO FECHAR O APLICATIVO COMPLETAMENTE E RECUPERAR
             * A ULTIMA MUSICA TOCADA.
             */
            if (playingState) {
                Log.e(getPackageName() + ":playback", "playing");

                preferencesUtil.storeAudioIndex(globalsUtil.audioIndex);

                if (globalsUtil.transportControls == null) {
                    startService(new Intent(this, MusicService.class));
                    PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
                } else {
                    broadcastUtils.playbackUIManager(BroadcastConstants.UpdateCover, false);
                    PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
                }
            } else {
                Log.e(getPackageName() + ":playback", "paused");
                bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);

                if (globalsUtil.transportControls == null) {
                    Log.e(getPackageName(), "transport controls null");
                    startService(new Intent(this, MusicService.class));
                    PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
                } else {
                    Log.e(getPackageName(), "transport controls not a null");
                    broadcastUtils.playbackUIManager(BroadcastConstants.UpdateCover, false);
                    PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_play));
                }
            }
        }
        preferencesUtil.clearCover();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(getBaseContext().getPackageName(), "onResume()");
        preferencesUtil.StoreUserInApp(true);

        LocalBroadcastManager
                .getInstance(this)
                .sendBroadcastSync(new Intent("user.state.view")
                .putExtra("user.state", 1)
                );


        if (globalsUtil.isServiceBound()) {
            if (globalsUtil.musicService.getPlayingState()) {
                Log.e(getBaseContext().getPackageName(), "service running");
                PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
                try {
                    updateCoverImage();
                    updateSeekBar();
                    startSeekBar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_play));
                try {
                    updateCoverImage();
                    updateSeekBar();
                    startSeekBar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferencesUtil.SetLastIndex(globalsUtil.audioIndex);

        unregisterReceiver(receivePlaying);
        unregisterReceiver(updateCover);

        Log.e(getPackageName(), "activity destroyed");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.e("savingInstance", "saved");
        outState.putBoolean("serviceStatus", globalsUtil.isServiceBound());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.e("onRestoreInstance", "called");
        globalsUtil.setServiceBound(savedInstanceState.getBoolean("serviceStatus"));
        super.onRestoreInstanceState(savedInstanceState);
    }
}