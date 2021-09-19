package com.drkryz.musicplayer.functions;


import android.annotation.SuppressLint;
import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.drkryz.musicplayer.utils.Song;

import java.util.ArrayList;

public class GetMusicsFromExt {

    private final ArrayList<Song> musics = new ArrayList<>();

    public GetMusicsFromExt populateSongs(Application app) {
        GetExternalContent(app);
        return null;
    }

    public Song getMusicIndex(int index) {
        return musics.get(index);
    }

    public ArrayList<Song> getAll() {
        return musics;
    }

    @SuppressLint("range")
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
                    String AlbumArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST));
                    Song song = new Song(title, author, path, duration, AlbumArt);

                    if (path.endsWith(".mp3")) musics.add(song);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }
}
