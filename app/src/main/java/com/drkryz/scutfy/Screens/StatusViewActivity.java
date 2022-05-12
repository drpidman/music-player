package com.drkryz.scutfy.Screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.drkryz.scutfy.Constants.BroadcastConstants;
import com.drkryz.scutfy.R;
import com.drkryz.scutfy.Services.MusicService;
import com.drkryz.scutfy.Utils.ServiceManagerUtil;

public class StatusViewActivity extends AppCompatActivity {

    private ImageView MEDIA_IMG;
    private TextView MEDIA_TITLE, MEDIA_AUTHOR;

    private ConstraintLayout StatusView;

    private MusicService musicService;
    private LocalBroadcastManager localBroadcastManager;


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.LocalBinder localBinder = (MusicService.LocalBinder) iBinder;
            musicService = localBinder.getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private boolean isRunning = false;

    private BroadcastReceiver MusicServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bindService(new Intent(getBaseContext(), MusicService.class), serviceConnection, BIND_AUTO_CREATE);
            isRunning = true;
        }
    };

    private BroadcastReceiver PlaybackStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateCoverImage();
        }
    };

    private void updateCoverImage() {
        if (musicService == null) return;

        MediaMetadata metadata = musicService.getMetadata();
        Bitmap cover = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

        MEDIA_IMG.setImageBitmap(cover);
        MEDIA_TITLE.setText(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        MEDIA_AUTHOR.setText(metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));

        MEDIA_TITLE.setSelected(true);

        Palette palette = new Palette.Builder(cover).generate();

        final int colorFrom = ((ColorDrawable) StatusView.getBackground()).getColor();
        final int colorTo = palette.getDominantColor(Color.TRANSPARENT);

        ObjectAnimator.ofObject(StatusView, "backgroundColor", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();

        Window window = getWindow();

        ObjectAnimator.ofObject(window, "statusBarColor", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();

        ObjectAnimator.ofObject(window, "navigationBarColor", new ArgbEvaluator(), colorFrom, colorTo)
                .setDuration(1000)
                .start();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_view);


        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.registerReceiver(PlaybackStatus, new IntentFilter(BroadcastConstants.UI_UPDATE_MEDIA_CONTROL_BUTTON));
        localBroadcastManager.registerReceiver(MusicServiceStatus, new IntentFilter(BroadcastConstants.PREPARE_CMD + ".running"));


        ServiceManagerUtil.handleAction(0, this);
        bindService(new Intent(this, MusicService.class), serviceConnection, BIND_AUTO_CREATE);



        MEDIA_IMG = (ImageView) findViewById(R.id.sc_mn_status_center_support_ImageView_AlbumImage);
        MEDIA_TITLE = (TextView) findViewById(R.id.sc_mn_status_center_support_MediaInfo_Title);
        MEDIA_AUTHOR = (TextView) findViewById(R.id.sc_mn_status_center_support_MediaInfo_Author);



        StatusView = (ConstraintLayout) findViewById(R.id.sc_mn_status);


    }

    @Override
    protected void onStart() {
        super.onStart();

        ServiceManagerUtil.handleAction(7, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.unregisterReceiver(PlaybackStatus);
        localBroadcastManager.unregisterReceiver(MusicServiceStatus);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.unregisterReceiver(PlaybackStatus);
        localBroadcastManager.unregisterReceiver(MusicServiceStatus);
    }

    @Override
    protected void onResume() {
        super.onResume();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        localBroadcastManager.registerReceiver(PlaybackStatus, new IntentFilter(BroadcastConstants.UI_UPDATE_MEDIA_CONTROL_BUTTON));
        localBroadcastManager.registerReceiver(MusicServiceStatus, new IntentFilter(BroadcastConstants.PREPARE_CMD + ".running"));


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateCoverImage();
            }
        }, 16);
    }
}