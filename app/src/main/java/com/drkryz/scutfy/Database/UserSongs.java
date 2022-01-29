package com.drkryz.scutfy.Database;

import android.provider.BaseColumns;

public final class UserSongs {


    private UserSongs() {}

    public static class UserSongsTable implements BaseColumns {
        public static final String TABLE_NAME = "user_songs";
        public static final String SONG_ID = "song_id";
        public static final String SONG_TITLE = "song_title";
        public static final String SONG_AUTHOR = "song_author";
        public static final String SONG_DURATION = "song_duration";
        public static final String SONG_ALBUM_URI = "song_album_uri";
        public static final String SONG_PATH_URI = "song_path_uri";
        public static final String SONG_FAVOURITE = "song_is_favorite";
    }
}
