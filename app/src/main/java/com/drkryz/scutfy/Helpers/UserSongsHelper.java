package com.drkryz.scutfy.Helpers;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.MediaStore;

import com.drkryz.scutfy.Class.Default.Encripta;
import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.drkryz.scutfy.Database.UserSongs.UserSongsTable;

import java.util.ArrayList;

public class UserSongsHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "playlist.db";

    private static final String SQL_ENTRIES =
            "CREATE TABLE " + UserSongsTable.TABLE_NAME + " (" +
                    UserSongsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UserSongsTable.SONG_TITLE + " TEXT, " +
                    UserSongsTable.SONG_AUTHOR + " TEXT, " +
                    UserSongsTable.SONG_DURATION + " TEXT, " +
                    UserSongsTable.SONG_ALBUM_URI + " TEXT, " +
                    UserSongsTable.SONG_PATH_URI + " TEXT, " +
                    UserSongsTable.SONG_FAVOURITE + " BOOLEAN)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserSongsTable.TABLE_NAME;

    private final ArrayList<UserPlaylist> loadedSong = new ArrayList<UserPlaylist>();

    public UserSongsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sql) {
        sql.execSQL(SQL_ENTRIES);
        sql.execSQL("PRAGMA auto_vacuum=FULL");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sql, int i, int i1) {
        sql.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sql);
    }

    public void storeAudio(SQLiteDatabase sql, UserPlaylist userPlaylist, ContentValues contentValues) {
        sql.insert(UserSongsTable.TABLE_NAME, null, contentValues);
        loadedSong.add(userPlaylist);

    }

    public ArrayList<UserPlaylist> getLoadedSong() {
        return loadedSong;
    }


    @SuppressLint("range")
    public void loadColumns(SQLiteDatabase sql, Context context) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(uri, null, MediaStore.Audio.Media.DURATION + ">= 60000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER, null);
        UserPlaylist userPlaylist;

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

                    try {
                        userPlaylist = new UserPlaylist(
                                new Encripta().encrypt(title), author, duration, path, String.valueOf(albumArt), false
                        );

                        loadedSong.add(userPlaylist);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } while (cursor.moveToNext());
                cursor.close();
            }
        }

    }

    public void updateAudio(SQLiteDatabase sql, UserPlaylist userPlaylist, ContentValues contentValues) {
        sql.update(UserSongsTable.TABLE_NAME, contentValues, null, null);
        loadedSong.add(userPlaylist);
    }

    public Cursor getAudio(SQLiteDatabase sql, String[] columns, int pos, String title, String author) {
        Cursor cursor = sql.query(
                UserSongsTable.TABLE_NAME, columns, UserSongsTable.SONG_TITLE + "='" + title + "'", null, null, null, null
        );

        return cursor;
    }

}
