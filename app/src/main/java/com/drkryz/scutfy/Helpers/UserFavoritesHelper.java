package com.drkryz.scutfy.Helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.drkryz.scutfy.Database.UserFavorites;

public class UserFavoritesHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "user_favorites.db";

    public UserFavoritesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLite) {
        sqLite.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    private static final String SQL_CREATE_ENTRIES
            = "CREATE TABLE " + UserFavorites.UserFavoritesValues.TABLE_NAME + " (_id INTEGER PRIMARY KEY, SONG_TITLE TEXT, SONG_PATH TEXT, IS_FAVORITE BOOLEAN, ALBUM_URI TEXT)";

    private static final String SQL_DELETE_ENTRIES
            = "DROP TABLE IF EXISTS " + UserFavorites.UserFavoritesValues.TABLE_NAME;



}
