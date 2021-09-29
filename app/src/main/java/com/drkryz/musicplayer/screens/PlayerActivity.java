package com.drkryz.musicplayer.screens;

import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION;
import static android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.GetMusicsFromExt;
import com.drkryz.musicplayer.listeners.ItemClickSupport;
import com.drkryz.musicplayer.listeners.MainListeners.TransitionListener;
import com.drkryz.musicplayer.listeners.OnSwipeTouchListener;
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


    private ImageButton PlayUiBtn;
    private View bottomNav;
    private ImageView coverImage;
    private MotionLayout motionLayout;
    private RecyclerView listView;
    private SeekBar seekBar;

    private TextView currentPlayingText;

    @SuppressLint({"ServiceCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(receivePlaying,
                new BroadcastUtils(getBaseContext()).playbackUIFilter(BroadcastConstants.RequestPlayChange)
        );

        registerReceiver(updateCover,
                new BroadcastUtils(getBaseContext()).playbackUIFilter(BroadcastConstants.UpdateCover)
        );

        registerReceiver(updateProgress,
                new BroadcastUtils(getBaseContext()).playbackUIFilter(BroadcastConstants.RequestProgress)
        );

        globalsUtil = (GlobalsUtil) getApplicationContext();
        globalsUtil.setServiceBound(false);

        // loadMusics
        GetMusicsFromExt externalGet = new GetMusicsFromExt();
        externalGet.populateSongs(getApplication());

        globalsUtil.setMusicList(externalGet.getAll());
        new PreferencesUtil(getBaseContext()).storePlayingState(false);

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


        listView = (RecyclerView) findViewById(R.id.musicList);
        bottomNav = (View) findViewById(R.id.bottomNavDrag);
        seekBar = (SeekBar) findViewById(R.id.progressBar);
        currentPlayingText = (TextView) findViewById(R.id.currentSongTitle);

        MusicRecyclerView musicRecyclerView = new MusicRecyclerView(globalsUtil.getMusicList(), this);

        listView.setAdapter(musicRecyclerView);
        listView.setHasFixedSize(true);

        listView.setLayoutManager(new LinearLayoutManager(this));


        ItemClickSupport.addTo(listView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Play(position);
                        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pause));
                    }
                });

        PlayUiBtn = (ImageButton) findViewById(R.id.uiPlay);
        coverImage = (ImageView) findViewById(R.id.musicAlbum);

        ImageButton UiPrevious = (ImageButton) findViewById(R.id.uiPrevious);

        UiPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Previous();
            }
        });

        ImageButton UiSkip = (ImageButton) findViewById(R.id.uiSkip);

        UiSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Skip();
            }
        });

        PlayUiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean state = new PreferencesUtil(getBaseContext()).loadPlayingState();
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

        ImageButton openWebPlayer = (ImageButton) findViewById(R.id.webPlayer);

        openWebPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent webViewPlayer = new Intent(getApplicationContext(), YTMusicActivity.class);
                startActivity(webViewPlayer);
            }
        });


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
            PreferencesUtil storage = new PreferencesUtil(getApplicationContext());

            storage.storageAudio(globalsUtil.getMusicList());
            storage.storeAudioIndex(position);

            Intent player = new Intent(getBaseContext(), MusicService.class);
            startService(player);
            bindService(player, serviceConnection, BIND_AUTO_CREATE);
        } else {
            PreferencesUtil storage = new PreferencesUtil(getApplicationContext());
            storage.storeAudioIndex(position);
            broadcastUtils.playbackUIManager(BroadcastConstants.Play, false);
        }
    }

    private boolean serviceState() {
        return globalsUtil.isServiceBound();
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

    private void updateSeekBar() {
        seekBar.setMax(globalsUtil.musicService.getTotalDuration());
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


    private void updateCoverImage() {
        Bitmap cover = null;

        try {
            cover = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    Uri.parse(globalsUtil.activeAudio.getAlbum())
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
                getDominantColor(cover)
        );

        ObjectAnimator.ofObject(motionLayout, "backgroundColor",
                new ArgbEvaluator(), colorFrom, colorTo
        )
                .setDuration(1000)
                .start();


        Drawable buttonPlay = PlayUiBtn.getBackground();

        ObjectAnimator.ofObject(buttonPlay, "tint", new ArgbEvaluator(), colorFrom,
                palette.getVibrantColor(getDominantColor(cover))
                )
                .setDuration(1000)
                .start();

        Drawable bottomListMusic = bottomNav.getBackground();

        ObjectAnimator.ofObject(bottomListMusic, "tint", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();


        Drawable listViewMusic = listView.getBackground();
        ObjectAnimator.ofObject(listViewMusic, "tint", new ArgbEvaluator(), colorFrom,
                getDominantColor(cover)
                )
                .setDuration(1000)
                .start();

        Window window = getWindow();
        
        ObjectAnimator.ofObject(window, "statusBarColor", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();

        ObjectAnimator.ofObject(window, "navigationBarColor", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();

        currentPlayingText.setSelected(true);
        currentPlayingText.setText(globalsUtil.activeAudio.getTitle());
    }


    // get color from bitmap cover
    private static int getDominantColor(Bitmap bitmap) {
        if (bitmap == null) return Color.TRANSPARENT;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int pixels[] = new int[size];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int color;
        int r = 0;
        int g = 0;
        int b = 0;
        int a;

        int count = 0;

        for(int i = 0; i < pixels.length; i++) {
            color = pixels[i];
            a = Color.alpha(color);
            if (a > 0) {
                r += Color.red(color);
                g += Color.green(color);
                b += Color.blue(color);
                count++;
            }
        }

        r /= count;
        g /= count;
        b /= count;
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = b & 0x000000FF;
        color = 0xFF000000 | r | g | b;

        return color;
    }


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
            updateCoverImage();
            updateSeekBar();
            startSeekBar();
        }
    };

    private final BroadcastReceiver updateProgress = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };


    @Override
    protected void onDestroy() {
        Log.d("onDestroy():main", "called");
        if (globalsUtil.isServiceBound()) {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

            unregisterReceiver(receivePlaying);
            unregisterReceiver(updateCover);

            unbindService(serviceConnection);
            globalsUtil.musicService.stopSelf();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                stopService(new Intent(this, MusicService.class));
            }

        }
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("serviceStatus", globalsUtil.isServiceBound());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        globalsUtil.setServiceBound(savedInstanceState.getBoolean("serviceStatus"));
    }
}
