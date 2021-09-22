package com.drkryz.musicplayer.screens;

import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION;
import static android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.GetMusicsFromExt;
import com.drkryz.musicplayer.listeners.ItemClickSupport;
import com.drkryz.musicplayer.listeners.MainListeners.TransitionListener;
import com.drkryz.musicplayer.listeners.OnSwipeTouchListener;
import com.drkryz.musicplayer.screens.adapters.MusicRecyclerView;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;
import com.drkryz.musicplayer.utils.StorageUtil;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    private GlobalVariables globalVariables;
    private BroadcastSenders broadcastSenders;


    ImageButton PlayUiBtn;
    ImageView coverImage;
    MotionLayout motionLayout;

    @SuppressLint({"ServiceCast", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        registerReceiver(receivePlaying,
                new BroadcastSenders(getBaseContext()).playbackUIFilter(BroadcastConstants.RequestPlayChange)
        );

        registerReceiver(updateCover,
                new BroadcastSenders(getBaseContext()).playbackUIFilter(BroadcastConstants.UpdateCover)
        );

        globalVariables = (GlobalVariables) getApplicationContext();
        globalVariables.setServiceBound(false);

        // loadMusics
        GetMusicsFromExt externalGet = new GetMusicsFromExt();
        externalGet.populateSongs(getApplication());

        globalVariables.setMusicList(externalGet.getAll());
        new StorageUtil(getBaseContext()).storePlayingState(false);

        motionLayout = (MotionLayout) findViewById(R.id.MainMotion);
        motionLayout.setTransitionListener(new TransitionListener(this));



        motionLayout.setOnTouchListener(new OnSwipeTouchListener() {
            @Override
            public boolean onSwipeLeft() {
                if (globalVariables.isServiceBound()) {
                    Skip();
                }
                return super.onSwipeLeft();
            }

            @Override
            public boolean onSwipeRight() {
                if (globalVariables.isServiceBound()) {
                    Previous();
                }
                return super.onSwipeRight();
            }
        });


        ImageButton EQButton = (ImageButton) findViewById(R.id.appSettings);

        RecyclerView listView = (RecyclerView) findViewById(R.id.musicList);

        MusicRecyclerView musicRecyclerView = new MusicRecyclerView(globalVariables.getMusicList(), this);

        listView.setAdapter(musicRecyclerView);

        listView.setLayoutManager(new LinearLayoutManager(this));


        ItemClickSupport.addTo(listView)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Play(position);
                        PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pausebtn));
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
                boolean state = new StorageUtil(getBaseContext()).loadPlayingState();
                Log.d("PLAYBACK", "" + state);
                if (state) {
                    Pause();
                    PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_playbtn));
                } else {
                    Resume();
                    PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pausebtn));
                }
            }
        });



        // create notification channel
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


        broadcastSenders = new BroadcastSenders(getApplicationContext());


        EQButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent audioEQ = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                audioEQ.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE_MUSIC);
                audioEQ.putExtra(EXTRA_AUDIO_SESSION, CONTENT_TYPE_MUSIC);

                if ((audioEQ.resolveActivity(getPackageManager()) != null)) {
                    startActivityForResult(audioEQ, 0);
                } else {

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
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(getPackageName(), "service connected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) iBinder;
            globalVariables.musicService = binder.getService();

            globalVariables.setServiceBound(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(getPackageName(), "service disconnected");
            globalVariables.setServiceBound(false);
        }
    };

    private void Play(int position) {
        if (!globalVariables.isServiceBound()) {
            StorageUtil storage = new StorageUtil(getApplicationContext());

            storage.storageAudio(globalVariables.getMusicList());
            storage.storeAudioIndex(position);

            Intent player = new Intent(getBaseContext(), MusicService.class);
            startService(player);
            bindService(player, serviceConnection, BIND_AUTO_CREATE);
        } else {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(position);
            broadcastSenders.playbackUIManager(BroadcastConstants.Play, false);
        }
    }

    private boolean serviceState() {
        return globalVariables.isServiceBound();
    }

    // controls ===============
    private void Pause() {
        if (!serviceState()) return;
        globalVariables.transportControls.pause();

    }

    private void Resume() {
        if (!serviceState()) return;
        globalVariables.transportControls.play();
    }

    private void Skip() {
        if (!serviceState()) return;
        globalVariables.transportControls.skipToNext();
    };

    private void Previous() {
        if (!serviceState()) return;
        globalVariables.transportControls.skipToPrevious();
    };
    // ===============================


    private void updateCoverImage() {
        Bitmap cover = null;

        try {
            cover = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    Uri.parse(globalVariables.activeAudio.getAlbum())
            );
        } catch (IOException e) {
            e.printStackTrace();
            cover = BitmapFactory.decodeResource(
                    getResources(),
                    R.drawable.default_music
            );
        }

        coverImage.setImageBitmap(cover);


        Bitmap finalCover = cover;

        // change status bar animated
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                getWindow().setStatusBarColor(
                        getDominantColor(finalCover)
                );
            }
        });

        animator.setDuration(2000);
        animator.start();

        // change background animated
        final int colorFrom = ((ColorDrawable) motionLayout.getBackground()).getColor();;
        final int colorTo = getDominantColor(cover);

        ObjectAnimator.ofObject(motionLayout, "backgroundColor",
                new ArgbEvaluator(), colorFrom, colorTo
                )
                .setDuration(1000)
                .start();
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
                PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_pausebtn));
            } else {
                PlayUiBtn.setImageDrawable(getDrawable(R.drawable.ui_playbtn));
            }
        }
    };

    private final BroadcastReceiver updateCover = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCoverImage();
        }
    };

    @Override
    protected void onDestroy() {
        if (globalVariables.isServiceBound()) {
            unbindService(serviceConnection);

            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();

            globalVariables.musicService.stopSelf();

            unregisterReceiver(receivePlaying);
            unregisterReceiver(updateCover);
        } else {
            globalVariables.mediaSession.release();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("serviceStatus", globalVariables.isServiceBound());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        globalVariables.setServiceBound(savedInstanceState.getBoolean("serviceStatus"));
    }
}