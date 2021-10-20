package com.drkryz.musicplayer.functions;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.drkryz.musicplayer.utils.SongUtil;

import java.util.ArrayList;

public class GetMusicsFromExt {

    private final ArrayList<SongUtil> musics = new ArrayList<>();

    public GetMusicsFromExt populateSongs(Application app) {
        GetExternalContent(app);
        return null;
    }

    public SongUtil getMusicIndex(int index) {
        return musics.get(index);
    }

    public ArrayList<SongUtil> getAll() {
        return musics;
    }

    @SuppressLint("Range")
    private void GetExternalContent(@NonNull Application app) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String isMusic = MediaStore.Audio.Media.IS_MUSIC + "!=0";


        Cursor cursor = app.getContentResolver().query(uri,null, isMusic, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    Long AlbumArt = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                    Uri artWork = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArt = ContentUris.withAppendedId(artWork, AlbumArt);

                    SongUtil song = new SongUtil(title, author, path, duration, String.valueOf(albumArt));

                   if (path.endsWith(".mp3")) {
                       if (duration != null) {
                           int durationFilter = Integer.parseInt(duration);

                           if (durationFilter > 5000) musics.add(song);
                       }
                   }

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }
}