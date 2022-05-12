package com.drkryz.scutfy.Database;

import android.provider.BaseColumns;

public final class UserFavorites {

    private UserFavorites() {};

    public static class UserFavoritesValues implements BaseColumns {
        public static final String TABLE_NAME = "FAVORITE";
        public static final String COLUMN_TITLE = "MUSIC_TITLE";
    }

}
