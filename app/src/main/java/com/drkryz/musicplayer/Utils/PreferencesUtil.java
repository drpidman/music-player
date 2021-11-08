package com.drkryz.musicplayer.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.drkryz.musicplayer.Class.Default.UserFavorites;
import com.drkryz.musicplayer.Class.Default.UserPlaylist;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PreferencesUtil {

    private final String STORAGE = "com.drkryz.musicplayer.STORAGE";
    private final String STORAGE_STATES = "com.drkryz.musicplayer.STORAGE_STATES";
    private final String STORAGE_USER = "com.drkryz.musicplayer.STORAGE_USER";

    private SharedPreferences preferences;
    private final Context context;

    public PreferencesUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<UserPlaylist> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audio.user.defaultPlaylist", json);
        editor.apply();
    }

    public ArrayList<UserPlaylist> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audio.user.defaultPlaylist", null);
        Type type = new TypeToken<ArrayList<UserPlaylist>>() {}.getType();

        return gson.fromJson(json, type);
    }


    public void storeFavorite(ArrayList<UserFavorites> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audio.user.favorites", json);
        editor.apply();
    }

    public ArrayList<UserFavorites> loadFavorite() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audio.user.favorites", null);
        Type type = new TypeToken<ArrayList<UserFavorites>>() {}.getType();

        return gson.fromJson(json, type);
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

    public boolean loadUserInApp() {
        preferences = context.getSharedPreferences(STORAGE_USER, Context.MODE_PRIVATE);
        return preferences.getBoolean("inApp", false);
    }
}