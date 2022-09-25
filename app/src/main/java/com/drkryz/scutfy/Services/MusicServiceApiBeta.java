package com.drkryz.scutfy.Services;

import static com.drkryz.scutfy.Constants.BroadcastConstants.PREPARE_CMD;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.PlayerId;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.drm.DrmSessionEventListener;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MusicServiceApiBeta extends Service implements
        Player.Listener
{

    public ExoPlayer player;


    private final  Binder iBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MusicServiceApiBeta getService() {
            return MusicServiceApiBeta.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        switch (action) {
            case "prepare":
                if (player == null) player = new ExoPlayer.Builder(getBaseContext()).build();

                if (player.getMediaItemCount() == 0) {
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    Cursor cursor = this.getBaseContext().getContentResolver().query(uri, null, MediaStore.Audio.Media.DURATION + ">= 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER, null);

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                                player.addMediaItem(new MediaItem.Builder().setMediaId(title).setUri(Uri.parse(path)).build());
                            } while (cursor.moveToNext());
                        }
                    }
                }
        }

        return super.onStartCommand(intent, flags, startId);
    }


    public void initPlayer() {

    }


    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if (isPlaying) {

        } else {

        }
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Player.Listener.super.onPlaybackStateChanged(playbackState);
    }
}
