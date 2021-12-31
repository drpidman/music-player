package com.drkryz.musicplayer.Utils;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.drkryz.musicplayer.Class.Default.UserFavorites;
import com.drkryz.musicplayer.Class.Default.UserPlaylist;
import com.drkryz.musicplayer.Screens.MusicActivity;
import com.drkryz.musicplayer.Utils.PreferencesUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class ContentManagerUtil {

    private final ArrayList<UserPlaylist> musics = new ArrayList<>();

    @SuppressLint("Range")
    public ContentManagerUtil(Context ctx) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String isMusic = MediaStore.Audio.Media.IS_MUSIC + "!=0";

        PreferencesUtil preferencesUtil = new PreferencesUtil(ctx);
        Cursor cursor = ctx.getContentResolver().query(uri, null, MediaStore.Audio.Media.DURATION + ">= 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER , null);

        int index = -1;

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                    long AlbumArt = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    Uri artWork = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArt = ContentUris.withAppendedId(artWork, AlbumArt);

                    index++;
                    UserPlaylist userPlaylist;

                    if (preferencesUtil.loadAudio() == null) {

                        userPlaylist = new UserPlaylist(
                                title, author, path, duration,
                                String.valueOf(albumArt), false
                        );

                    } else {

                        ArrayList<UserPlaylist> comparator = preferencesUtil.loadAudio();

                        if (index != -1 && index < comparator.size()) {
                            userPlaylist = comparator.get(index);
                        } else {
                            userPlaylist = new UserPlaylist(
                                    title, author, path, duration,
                                    String.valueOf(albumArt), false);
                        }


                        userPlaylist = new UserPlaylist(
                                title, author, path, duration,
                                String.valueOf(albumArt), userPlaylist.isFavorite()
                        );

                        Log.e(userPlaylist.getTitle(), "favorited?" + cursor.getPosition());
                    }

                    musics.add(userPlaylist);

                } while (cursor.moveToNext());

                cursor.close();
            }
        }
    }

    public ArrayList<UserPlaylist> getMusics() {
        return musics;
    }
}