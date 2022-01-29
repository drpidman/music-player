package com.drkryz.scutfy.Database;

import android.provider.BaseColumns;

public final class UserFavorites {

    private UserFavorites() {};

    public static class UserFavoritesValues implements BaseColumns {
        public static final String TABLE_NAME = "FAVORITE";
        public static final String COLUMN_TITLE = "SONG_TITLE";
        public static final String COLUMN_PATH = "SONG_PATH";
        public static final String COLUMN_FAVORITE = "IS_FAVORITE";
        public static final String COLUMN_ALBUM_URI = "ALBUM_URI";
    }

}
