package com.drkryz.scutfy.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.drkryz.scutfy.Class.Default.UserPlaylist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PreferencesUtil {

    private final String STORAGE = "com.drkryz.musicplayer.STORAGE";
    private final String STORAGE_STATES = "com.drkryz.musicplayer.playback_state";
    private final String STORAGE_USER = "com.drkryz.musicplayer.STORAGE_USER";


    private final Context context;
    private SharedPreferences preferences;

    public PreferencesUtil(Context context) {
        this.context = context;
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);
    }


    public void storeAudioTrackData(String track) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("audioTrack", track);
        editor.apply();
    }

    public String loadAudioTrackData() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("audioTrack", "track");
    }


    public void storeAudioTitle(String title) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("audio.title", title);
        editor.apply();
    }

    public String loadAudioTitle() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("audio.title", "");
    }


    public void storePlayingState(boolean playingState) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("playingState", playingState);
        editor.apply();
    }

    public boolean getPlayingState() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getBoolean("playingState", false);

    }


    public void pausedByUser(boolean paused) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("pausedByUser", false);
        editor.apply();
    }


    public boolean getPausedByUserState() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getBoolean("pausedByUser", false);

    }


    public void setLastPosition(int index) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("lastIndex", index);
        editor.apply();
    }

    public int loadLastPosition() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getInt("lastIndex", 0);
    }


    public void storeUserInApp(boolean inactive) {
        preferences = context.getSharedPreferences(STORAGE_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("inApp", inactive);
        editor.apply();
    }

    public void storeShuffleState(boolean shuffle) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("state.shuffle", shuffle);
        editor.apply();
    }


    public void setInitFirst(boolean firstInit) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("state.first", firstInit);
        editor.apply();
    }

    public boolean getFirstInit() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getBoolean("state.first", false);
    }


    public boolean loadShuffleState() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getBoolean("state.shuffle", false);
    }

    public void storeLoopState(boolean loop) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("state.loop", loop);
        editor.apply();
    }

    public boolean loadLoopState() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getBoolean("state.loop", false);
    }
}