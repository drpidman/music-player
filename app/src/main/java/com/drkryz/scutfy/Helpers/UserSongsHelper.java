package com.drkryz.scutfy.Helpers;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.drkryz.scutfy.Class.Default.Encripta;
import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.drkryz.scutfy.Database.UserSongs.UserSongsTable;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class UserSongsHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "playlist.db";

    private static final String SQL_ENTRIES =
            "CREATE TABLE " + UserSongsTable.TABLE_NAME + " (" +
                    UserSongsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + UserSongsTable.SONG_COLUMN + " TEXT " +
                    ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserSongsTable.TABLE_NAME;

    private final ArrayList<UserPlaylist> loadedSong = new ArrayList<UserPlaylist>();

    public UserSongsHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sql) {
        sql.execSQL(SQL_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sql, int i, int i1) {
        sql.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sql);
    }

    public void storeAudio(SQLiteDatabase sql, ContentValues contentValues) {
        if (loadAudio(sql) != null) {
            sql.update(UserSongsTable.TABLE_NAME, contentValues, null, null);
        } else {
            sql.insert(UserSongsTable.TABLE_NAME, null, contentValues);
        }
    }

    public ArrayList<UserPlaylist> loadAudio(SQLiteDatabase sql) {
        Gson gson
                = new Gson();


        Cursor cursor = sql.query(UserSongsTable.TABLE_NAME,
                new String[]{ UserSongsTable.SONG_COLUMN }, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String JSONString = cursor.getString(cursor.getColumnIndex(UserSongsTable.SONG_COLUMN));

            Type type = new TypeToken<ArrayList<UserPlaylist>>() {}.getType();
            return gson.fromJson(JSONString, type);
        }

        return null;
    }
}
