package com.drkryz.musicplayer.screens;

import static android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC;
import static android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION;
import static android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.GetMusicsFromExt;
import com.drkryz.musicplayer.listeners.MainListeners.TransitionListener;
import com.drkryz.musicplayer.screens.adapters.MusicListAdapter;
import com.drkryz.musicplayer.services.MusicService;
import com.drkryz.musicplayer.utils.BroadcastConstants;
import com.drkryz.musicplayer.utils.BroadcastSenders;
import com.drkryz.musicplayer.utils.GlobalVariables;
import com.drkryz.musicplayer.utils.StorageUtil;

public class MainActivity extends AppCompatActivity {


    private GlobalVariables globalVariables;
    private BroadcastSenders broadcastSenders;
    private MediaBrowserCompat mMediaBrowser;

    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        globalVariables = (GlobalVariables) getApplicationContext();
        globalVariables.setServiceBound(false);

        // loadMusics
        GetMusicsFromExt externalGet = new GetMusicsFromExt();
        externalGet.populateSongs(getApplication());

        globalVariables.setMusicList(externalGet.getAll());

        MotionLayout motionLayout = (MotionLayout) findViewById(R.id.MainMotion);
        motionLayout.setTransitionListener(new TransitionListener(this));

        ImageButton EQButton = (ImageButton) findViewById(R.id.appSettings);

        ListView listView = (ListView) findViewById(R.id.musicList);
        listView.setAdapter(new MusicListAdapter(globalVariables.getMusicList(), this));


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Play(i);
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

            broadcastSenders.playbackUIManager(BroadcastConstants.Play);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(getPackageName(), "activity paused");
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d(getPackageName(), "activity leave hint");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getPackageName(), "activity resume");
    }

    @Override
    protected void onDestroy() {
        if (globalVariables.isServiceBound()) {
            unbindService(serviceConnection);

            broadcastSenders.playbackManager(BroadcastConstants.RequestDestroy);

            globalVariables.musicService.stopForeground(true);
        } else {
            globalVariables.mediaSession.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("serviceStatus", globalVariables.isServiceBound());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        globalVariables.setServiceBound(savedInstanceState.getBoolean("serviceStatus"));
    }
}