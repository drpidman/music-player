package com.drkryz.musicplayer.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadata;
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
import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.functions.ServiceManager;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.utils.PreferencesUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {


    private MusicService musicService;
    private PreferencesUtil preferencesUtil;

    private boolean serviceBound = false;
    private boolean isRunning = false;
    private boolean isPlaying = false;

    private ImageButton playButton, skipButton, prevButton, closePlayerButton;
    private ImageView musicAlbumArt;
    private TextView musicTitle, musicArtist, mediaCurrentPosition, mediaTotalDuration;
    private SeekBar seekBar;

    private ConstraintLayout loadingScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(PlaybackStatusReceiver, new IntentFilter(BroadcastConstants.UI_UPDATE_MEDIA_CONTROL_BUTTON));

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(MusicServiceStatus, new IntentFilter(BroadcastConstants.PREPARE_CMD + ".running"));


        musicAlbumArt = (ImageView) findViewById(R.id.mediaAlbumArt);
        musicTitle = (TextView) findViewById(R.id.mediaCurrentTitle);
        musicArtist = (TextView) findViewById(R.id.mediaCurrentAuthor);
        mediaCurrentPosition = (TextView) findViewById(R.id.mediaCurrentPosition);
        mediaTotalDuration = (TextView) findViewById(R.id.mediaTotalDuration);

        seekBar = (SeekBar) findViewById(R.id.appCompatSeekBar);
        skipButton = (ImageButton) findViewById(R.id.mediaSkip);
        prevButton = (ImageButton) findViewById(R.id.mediaPrev);
        playButton = (ImageButton) findViewById(R.id.mediaPlay);
        closePlayerButton = (ImageButton) findViewById(R.id.closePlayerUi);
        loadingScreen = (ConstraintLayout) findViewById(R.id.loadingView);
        preferencesUtil = new PreferencesUtil(getBaseContext());


        bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);
        ServiceManager.handleAction(0, this);

        playButton.setOnClickListener(new View.OnClickListener() {
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

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
                    ServiceManager.handleAction(5, getBaseContext());
                } else {
                    ServiceManager.handleAction(5, getBaseContext());
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
                    ServiceManager.handleAction(6, getBaseContext());
                } else {
                    ServiceManager.handleAction(6, getBaseContext());
                }
            }
        });

        closePlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), MusicActivity.class));
                finish();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                if (fromUser) {
                    musicService.getTransportControls().seekTo(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
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

                    if (isPlaying) {
                        playButton.setImageDrawable(getDrawable(R.drawable.ui_pause));
                        try {
                            updateCoverImage();
                            startSeekBar();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        playButton.setImageDrawable(getDrawable(R.drawable.ui_play));
                        try {
                            updateCoverImage();
                            startSeekBar();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                loadingScreen.setVisibility(View.INVISIBLE);
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

        unbindService(serviceConnection);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(MusicServiceStatus);
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(PlaybackStatusReceiver);

        preferencesUtil.StoreUserInApp(true);

    }

    private void updateSeekBar() {
        MediaMetadata mediaMetadata = musicService.getMetadata();
        seekBar.setMax((int) mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION));
        mediaTotalDuration.setText(getTime((int) mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION)));
    }

    private final Handler mHandler = new Handler();
    private void startSeekBar() {

        if (!preferencesUtil.LoadUserInApp()) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService == null) return;
                if (!preferencesUtil.LoadUserInApp()) return;
                seekBar.setProgress(musicService.getCurrentPosition());
                mediaCurrentPosition.setText(getTime(musicService.getCurrentPosition()));

                mHandler.postDelayed(this, 1000);
            }
        });
    }

    private void updateCoverImage() throws IOException {
        if (!preferencesUtil.LoadUserInApp()) return;

        MediaMetadata metadata = musicService.getMetadata();
        Bitmap cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        cover.compress(Bitmap.CompressFormat.JPEG, 100, out);
        Bitmap decode = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));


        musicAlbumArt.setImageBitmap(decode);
        musicTitle.setSelected(true);
        musicTitle.setText(
                metadata.getText(MediaMetadata.METADATA_KEY_TITLE)
        );

        musicArtist.setSelected(true);
        musicArtist.setText(
                metadata.getText(MediaMetadata.METADATA_KEY_ARTIST)
        );

        loadingScreen.setVisibility(View.INVISIBLE);

        updateSeekBar();
    }

    public String getTime(int time) {
        int seconds = time / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }


    private final BroadcastReceiver PlaybackStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isPlaying = intent.getBooleanExtra("playback.status", false);

            Log.e(getPackageName(), "PlayerActivity():status received=" + isPlaying);
            if (isPlaying) {
                playButton
                        .setImageDrawable(getDrawable(R.drawable.ui_pause));
                try {
                    updateCoverImage();
                    startSeekBar();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                playButton
                        .setImageDrawable(getDrawable(R.drawable.ui_play));
            }
        }
    };

    private final BroadcastReceiver MusicServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            isRunning = true;
        }
    };

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
            serviceBound = false;
        }
    };
}