package com.drkryz.scutfy.Screens;

import android.animation.ArgbEvaluator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;

import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.drkryz.scutfy.Constants.BroadcastConstants;
import com.drkryz.scutfy.Helpers.UserFavoritesHelper;
import com.drkryz.scutfy.R;
import com.drkryz.scutfy.Services.MusicService;
import com.drkryz.scutfy.Utils.ContentManagerUtil;
import com.drkryz.scutfy.Utils.PreferencesUtil;
import com.drkryz.scutfy.Utils.ServiceManagerUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import jp.wasabeef.blurry.Blurry;

public class PlayerActivity extends AppCompatActivity {


    private final Handler mHandler = new Handler();
    private MusicService musicService;
    private PreferencesUtil preferencesUtil;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(getPackageName(), "service connected");
            MusicService.LocalBinder binder = (MusicService.LocalBinder) iBinder;
            musicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


    private boolean isRunning = false;


    private final BroadcastReceiver MusicServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            isRunning = true;
        }
    };


    private boolean isPlaying = false;


    private ImageButton
            playButton, skipButton, prevButton,
            closePlayerButton, favoriteButton,
            shuffleButton, loopingButton, shareButton;
    private ImageView musicAlbumArt;
    private TextView musicTitle, musicArtist, mediaCurrentPosition, mediaTotalDuration;
    private SeekBar seekBar;
    private ConstraintLayout loadingScreen, blureableSupportView, playerView;


    private UserFavoritesHelper userFavoritesHelper;


    private final BroadcastReceiver PlaybackStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isPlaying = intent.getBooleanExtra("playback.status", false);

            try {
                updateCoverImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    private GradientDrawable gradientDrawable;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
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


        musicAlbumArt = findViewById(R.id.mediaAlbumArt);
        musicTitle = findViewById(R.id.mediaCurrentTitle);
        musicArtist = findViewById(R.id.mediaCurrentAuthor);
        mediaCurrentPosition = findViewById(R.id.mediaCurrentPosition);
        mediaTotalDuration = findViewById(R.id.mediaTotalDuration);

        seekBar = findViewById(R.id.appCompatSeekBar);
        skipButton = findViewById(R.id.mediaSkip);
        prevButton = findViewById(R.id.mediaPrev);
        playButton = findViewById(R.id.mediaPlay);
        shareButton = findViewById(R.id.shareTo);
        favoriteButton = findViewById(R.id.AddFavorite);
        shuffleButton = findViewById(R.id.mediaShuffle);
        loopingButton = findViewById(R.id.mediaLooping);


        closePlayerButton = findViewById(R.id.closePlayerUi);
        loadingScreen = findViewById(R.id.loadingView);


        blureableSupportView = findViewById(R.id.blureableSupportView);
        playerView = findViewById(R.id.playerView);
        preferencesUtil = new PreferencesUtil(getBaseContext());


        bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);
        ServiceManagerUtil.handleAction(0, this);

        userFavoritesHelper = new UserFavoritesHelper(this);


        musicAlbumArt.setOnClickListener(view -> {
            startActivity(new Intent(PlayerActivity.this, StatusViewActivity.class));
        });


        playButton.setOnClickListener(view -> {
            int audioIndex = preferencesUtil.loadAudioIndex();

            if (isRunning) {
                if (isPlaying) {
                    ServiceManagerUtil.handleAction(3, getBaseContext());
                    preferencesUtil.pausedByUser(true);
                } else {
                    ServiceManagerUtil.handleAction(4, getBaseContext());
                    preferencesUtil.pausedByUser(false);
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

        skipButton.setOnClickListener(view -> {
            if (!isRunning) {
                bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            }

            ServiceManagerUtil.handleAction(5, getBaseContext());
        });

        prevButton.setOnClickListener(view -> {
            if (!isRunning) {
                bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            }

            ServiceManagerUtil.handleAction(6, getBaseContext());
        });


        closePlayerButton.setOnClickListener(view -> {
            Activity activity = (Activity) view.getContext();

            startActivity(new Intent(getBaseContext(), MusicActivity.class), ActivityOptions
                    .makeSceneTransitionAnimation(activity).toBundle()
            );

            finish();
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

        favoriteButton.setOnClickListener(view -> {
            if (musicService == null) return;


            MediaMetadata metadata = musicService.getMetadata();
            boolean isFavorite = userFavoritesHelper.isFavorite(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));


            ServiceManagerUtil.handleAction(8, getBaseContext());

            if (isFavorite) {
                favoriteButton.setImageDrawable(AppCompatResources.getDrawable(getBaseContext(), R.drawable.btn_favorite));
            } else {
                favoriteButton.setImageDrawable(AppCompatResources.getDrawable(getBaseContext(), R.drawable.btn_favorite_active));
                Drawable fab = favoriteButton.getDrawable();
                fab.setTint(getColor(R.color.purple));
            }
        });


        shuffleButton.setOnClickListener(view -> {
            if (musicService == null) return;
            if (preferencesUtil.loadShuffleState()) {
                ServiceManagerUtil.handleAction(9, getBaseContext());
                shuffleButton.setImageDrawable(AppCompatResources.getDrawable(getBaseContext(), R.drawable.btn_shuffle));
            } else {
                ServiceManagerUtil.handleAction(9, getBaseContext());
                Drawable fab = shuffleButton.getDrawable();
                fab.setTint(getColor(R.color.purple));
            }
        });

        loopingButton.setOnClickListener(view -> {
            if (musicService == null) return;
            if (preferencesUtil.loadLoopState()) {
                ServiceManagerUtil.handleAction(10, getBaseContext());
                loopingButton.setImageDrawable(AppCompatResources.getDrawable(getBaseContext(), R.drawable.nf_repeat));
            } else {
                ServiceManagerUtil.handleAction(10, getBaseContext());
                Drawable fab = loopingButton.getDrawable();
                fab.setTint(getColor(R.color.purple));
            }
        });

        shareButton.setOnClickListener(view -> {


            MediaMetadata mediaMetadata = musicService.getMetadata();

            Intent shareIntentAudio = new Intent();
            shareIntentAudio.setAction(Intent.ACTION_SEND);
            shareIntentAudio.putExtra(Intent.EXTRA_TITLE, mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE));
            shareIntentAudio.putExtra(Intent.EXTRA_TEXT, mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE));
            shareIntentAudio.putExtra(Intent.EXTRA_STREAM, Uri.parse(mediaMetadata.getString(MediaMetadata.METADATA_KEY_MEDIA_URI)));
            shareIntentAudio.setType(mediaMetadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID));
            startActivity(Intent.createChooser(shareIntentAudio, "Enviar para..."));
        });


        final int colorFrom = ((ColorDrawable) playerView.getBackground()).getColor();


        gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{colorFrom, colorFrom}
        );

        playerView.setBackground(gradientDrawable);
    }

    @Override
    protected void onStart() {
        super.onStart();


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
                startSeekBar();
            }

        }, 0);
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


        startActivity(new Intent(this, MusicActivity.class), ActivityOptions
                .makeSceneTransitionAnimation(this).toBundle()
        );

        preferencesUtil.storeUserInApp(true);

    }

    private void updateSeekBar() {
        MediaMetadata mediaMetadata = musicService.getMetadata();
        seekBar.setMax((int) mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION));
        mediaTotalDuration.setText(getTime((int) mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION)));
    }

    private void startSeekBar() {
        if (musicService == null) return;


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(musicService.getCurrentPosition());
                mediaCurrentPosition.setText(getTime(musicService.getCurrentPosition()));

                mHandler.postDelayed(this, 1000);
            }
        });
    }

    private void updateCoverImage() throws IOException {

        MediaMetadata metadata = musicService.getMetadata();
        if (metadata == null) return;

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


        boolean isFavorite = userFavoritesHelper.isFavorite(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));

        if (isFavorite) {
            favoriteButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.btn_favorite_active));

            Drawable fab = favoriteButton.getDrawable();
            fab.setTint(getColor(R.color.purple));

        } else {
            favoriteButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.btn_favorite));
        }

        if (!preferencesUtil.loadShuffleState()) {
            shuffleButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.btn_shuffle));
        } else {
            Drawable fab = shuffleButton.getDrawable();
            fab.setTint(getColor(R.color.purple));
        }

        if (!preferencesUtil.loadLoopState()) {
            loopingButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.nf_repeat));
        } else {
            Drawable fab = loopingButton.getDrawable();
            fab.setTint(getColor(R.color.purple));
        }


        Palette palette = new Palette.Builder(cover).generate();


        final int colorTo = palette.getDominantColor(Color.TRANSPARENT);
        final int dark = Color.BLACK;


        ValueAnimator valueAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), dark, colorTo);
        valueAnimator.setDuration(1000);

        if (isPlaying) {
            playButton.setBackgroundResource(R.drawable.sc_mn_anim_playback_play);

            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    gradientDrawable.setColors(new int[]{dark, (int) animation.getAnimatedValue()});
                    getWindow().setStatusBarColor((int) animation.getAnimatedValue());
                    getWindow().setNavigationBarColor(dark);
                }
            });


        } else {

        playButton.setBackgroundResource(R.drawable.sc_mn_anim_playback_pause);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                gradientDrawable.setColors(new int[]{dark, dark});
                getWindow().setStatusBarColor(dark);
                getWindow().setNavigationBarColor(dark);
            }
        });


        }
        valueAnimator.start();


        AnimationDrawable PLAY_PAUSE_ANIMATION = (AnimationDrawable) playButton.getBackground();
        PLAY_PAUSE_ANIMATION.start();



        updateSeekBar();
    }

    @SuppressLint("DefaultLocale")
    public String getTime(int time) {
        int seconds = time / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    static {
        System.loadLibrary("scutfy-msp-c");
    }
}