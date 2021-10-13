package com.drkryz.musicplayer.screens;

import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION;
import static android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.palette.graphics.Palette;
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
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.DominantColor;
import com.drkryz.musicplayer.functions.GetMusicsFromExt;
import com.drkryz.musicplayer.listeners.ItemClickSupport;
import com.drkryz.musicplayer.listeners.MainListeners.TransitionListener;
import com.drkryz.musicplayer.listeners.OnSwipeTouchListener;
import com.drkryz.musicplayer.managers.MusicManager;
import com.drkryz.musicplayer.managers.NotificationBuilderManager;
import com.drkryz.musicplayer.screens.adapters.MusicRecyclerView;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastUtils;
import com.drkryz.musicplayer.utils.GlobalsUtil;
import com.drkryz.musicplayer.utils.PreferencesUtil;

import java.io.IOException;

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


        ImageButton EQButton = (ImageButton) findViewById(R.id.appSettings);
        ImageButton UiPrevious = (ImageButton) findViewById(R.id.uiPrevious);
        ImageButton openWebPlayer = (ImageButton) findViewById(R.id.webPlayer);
        ImageButton UiSkip = (ImageButton) findViewById(R.id.uiSkip);

        currentPlayingText = (TextView) findViewById(R.id.currentSongTitle);
        listView = (RecyclerView) findViewById(R.id.musicList);
        bottomNav = (View) findViewById(R.id.bottomNavDrag);
        seekBar = (SeekBar) findViewById(R.id.progressBar);
        PlayUiBtn = (ImageButton) findViewById(R.id.uiPlay);
        coverImage = (ImageView) findViewById(R.id.musicAlbum);

        // list view adapter
        MusicRecyclerView musicRecyclerView = new MusicRecyclerView(globalsUtil.getMusicList(), this);
        listView.setAdapter(musicRecyclerView);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setItemViewCacheSize(1);

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


        EQButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent audioEQ = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                audioEQ.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE_MUSIC);
                audioEQ.putExtra(EXTRA_AUDIO_SESSION, CONTENT_TYPE_MUSIC);

                if ((audioEQ.resolveActivity(getPackageManager()) != null)) {
                    startActivityForResult(audioEQ, 0);
                }
            }
        });

        openWebPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent webViewPlayer = new Intent(getApplicationContext(), YTMusicActivity.class);
                startActivity(webViewPlayer);
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
                    seekBar.setProgress(globalsUtil.musicService.getCurrentPosition());
                    mHandler.postDelayed(this, 1000);
                }
            });
        }
    }


    private static Bitmap cover = null;
    private void updateCoverImage() throws IOException {
        if (!serviceState()) return;

        globalsUtil.audioIndex = preferencesUtil.loadAudioIndex();
        globalsUtil.activeAudio = globalsUtil.getMusicList().get(globalsUtil.audioIndex);
        String albumUri = globalsUtil.activeAudio.getAlbum();

        try {
            cover = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    Uri.parse(albumUri)
            );
        } catch (IOException e) {
            e.printStackTrace();
            cover = BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.default_music
            );
        }

        coverImage.setImageBitmap(cover);


        Palette palette = new Palette.Builder(cover).generate();

        // change background animated
        final int colorFrom = ((ColorDrawable) motionLayout.getBackground()).getColor();
        final int colorTo = palette.getDominantColor(
                DominantColor.GetDominantColor(cover)
        );


        Drawable buttonPlay = PlayUiBtn.getBackground();
        Drawable bottomListMusic = bottomNav.getBackground();

        Window window = getWindow();

        ObjectAnimator.ofObject(motionLayout, "backgroundColor",
                new ArgbEvaluator(), colorFrom, colorTo
        )
                .setDuration(1000)
                .start();

        ObjectAnimator.ofObject(buttonPlay, "tint", new ArgbEvaluator(), colorFrom,
                palette.getVibrantColor(DominantColor.GetDominantColor(cover))
        )
                .setDuration(1000)
                .start();

        ObjectAnimator.ofObject(bottomListMusic, "tint", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();

        ObjectAnimator.ofObject(window, "statusBarColor", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();

        ObjectAnimator.ofObject(window, "navigationBarColor", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();


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
        globalsUtil.transportControls.pause();
        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_play));
        seekBar.setVisibility(View.INVISIBLE);
    }

    private void Resume() {
        if (!serviceState()) return;
        globalsUtil.transportControls.play();
        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
        seekBar.setVisibility(View.VISIBLE);
    }

    private void Skip() {
        if (!serviceState()) return;
        globalsUtil.transportControls.skipToNext();
        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
        seekBar.setVisibility(View.VISIBLE);
    };

    private void Previous() {
        if (!serviceState()) return;
        globalsUtil.transportControls.skipToPrevious();
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
        if (preferencesUtil.GetFirstInit()) {
            Log.e(getPackageName(), "retomando a musica anterior");

            boolean playingState = preferencesUtil.GetPlayingState();

            /**
             * RESUMIR ESTADO ANTERIOR DA VIEW.
             * SE APLICA QUANDO O USUARIO FECHAR O APLICATIVO PELO BOTÃO "voltar" fisico ou virtual.
             * SE APLICA QUANDO O USUARIO FECHAR O APLICATIVO COMPLETAMENTE E RECUPERAR
             * A ULTIMA MUSICA TOCADA.
             */
            if (playingState) {
                Log.e(getPackageName() + ":playback", "playing");

                preferencesUtil.storeAudioIndex(globalsUtil.audioIndex);
                bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);

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
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(getBaseContext().getPackageName(), "onResume()");
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
