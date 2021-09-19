package com.drkryz.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.logging.Handler;

public class StorageUtil {

    private final String STORAGE = "com.drkryz.musicplayer.STORAGE";
    private final String STATE = "com.drkryz.musicplayer.STATES";

    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storageAudio(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<Song> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<Song>>() {}.getType();

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

    public void clearCachedAudioPlaylist() {
        Log.d("clearCachedAudioPlaylist()", "called");
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();
    }

    public void storePlayingState(boolean state) {
        preferences = context.getSharedPreferences(STATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("isPlaying", state);
        editor.apply();
    }

    public boolean loadPlayingState() {
        preferences = context.getSharedPreferences(STATE, context.MODE_PRIVATE);
        return preferences.getBoolean("isPlaying", false);
    }

    public void clearCachedPlayingStatus() {
        preferences = context.getSharedPreferences(STATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();
    }

}
