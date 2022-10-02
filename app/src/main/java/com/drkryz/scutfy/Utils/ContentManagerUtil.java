package com.drkryz.scutfy.Utils;


import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.drkryz.scutfy.Database.UserSongs;
import com.drkryz.scutfy.Helpers.UserSongsHelper;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ContentManagerUtil {

    private final ArrayList<UserPlaylist> musics = new ArrayList<>();

    @SuppressLint("Range")
    public ContentManagerUtil(Context ctx) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = ctx.getContentResolver().query(uri, null, MediaStore.Audio.Media.DURATION + ">= 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER, null);

        int index = -1;

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    String type = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));

                    long AlbumArt = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    Uri artWork = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArt = ContentUris.withAppendedId(artWork, AlbumArt);

                    index++;
                    UserPlaylist userPlaylist = new UserPlaylist(
                            title, author, path, duration,
                            String.valueOf(albumArt), false, type
                    );


                    musics.add(userPlaylist);

                } while (cursor.moveToNext());

                cursor.close();
            }
        }
    }


    @SuppressLint("Range")
    public String getMusicPathByName(String music_title, Context ctx) {


        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] selection = {
                music_title
        };

        Cursor cursor = ctx.getContentResolver()
                .query(uri, null, MediaStore.Audio.Media.TITLE + "=?", selection, null, null);


        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        }

        return null;
    }

    public int getMusicIndexByName(String music_title, Context ctx) {

        int index = -1;

        for (UserPlaylist music : musics) {
            index++;
            if (music.getTitle().equals(music_title)) {
                return index;
            }
        }

        return -1;
    }



    


    public ArrayList<UserPlaylist> getMusics(Context ctx) {
        return musics;
    }
}