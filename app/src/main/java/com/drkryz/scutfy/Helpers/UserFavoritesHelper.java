package com.drkryz.scutfy.Helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.drkryz.scutfy.Class.Default.Encripta;
import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.drkryz.scutfy.Database.UserFavorites;

public class UserFavoritesHelper extends SQLiteOpenHelper {




    private Encripta encripta;


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
            = "CREATE TABLE " + UserFavorites.UserFavoritesValues.TABLE_NAME + "(" +
            "_id INTEGER PRIMARY KEY, " +
            UserFavorites.UserFavoritesValues.COLUMN_TITLE + " TEXT " + ")";

    private static final String SQL_DELETE_ENTRIES
            = "DROP TABLE IF EXISTS " + UserFavorites.UserFavoritesValues.TABLE_NAME;


    private void addDatabaseFav(String music_title, SQLiteDatabase sqLiteDatabase) throws Exception {

        encripta = new Encripta();

        String newMusicTitle = encripta.encrypt(music_title);
        Log.d("encrypt", newMusicTitle);

        ContentValues contentValues = new ContentValues();
        contentValues.put(UserFavorites.UserFavoritesValues.COLUMN_TITLE, newMusicTitle);

        sqLiteDatabase.insert(
                UserFavorites.UserFavoritesValues.TABLE_NAME,
                null,
                contentValues
        );
    }

    private void removeDatabaseFav(String  music_title, SQLiteDatabase sqLiteDatabase) throws Exception {
        encripta = new Encripta();

        String newMusicTitle = encripta.encrypt(music_title);

        String[] selection = { newMusicTitle };

        sqLiteDatabase.delete(
                UserFavorites.UserFavoritesValues.TABLE_NAME,
                UserFavorites.UserFavoritesValues.COLUMN_TITLE + "=?",
                selection
        );

    }

    @SuppressLint("Range")
    private String getMusic(String music_title, SQLiteDatabase sqLiteDatabase) throws Exception {

        encripta = new Encripta();

        String newMusicTitle = encripta.encrypt(music_title);


        String[] selection = {
                newMusicTitle
        };

        Cursor cursor = sqLiteDatabase.query(
                UserFavorites.UserFavoritesValues.TABLE_NAME,
                new String[] {
                        UserFavorites.UserFavoritesValues.COLUMN_TITLE
                }, UserFavorites.UserFavoritesValues.COLUMN_TITLE + "=?",
                selection, null, null, null
        );

        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(UserFavorites.UserFavoritesValues.COLUMN_TITLE));
        }

        return "";
    };


    public void storeFavorite(String music_title) {
        try {
            addDatabaseFav(music_title, this.getWritableDatabase());
        } catch (Exception e) {
            Log.e("Failed insert", e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeFavorite(String music_title) {
        try {
            removeDatabaseFav(music_title, this.getWritableDatabase());
        } catch (Exception e) {
            Log.e("Failed remove", e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isFavorite(String music_title) {
        try {
            return !getMusic(music_title, this.getWritableDatabase()).equals("");
        } catch (Exception e) {
            Log.e("Failed to get", e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
