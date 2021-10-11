package com.drkryz.musicplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PreferencesUtil {

    private final String STORAGE = "com.drkryz.musicplayer.STORAGE";
    private final String STORAGE_STATES = "com.drkryz.musicplayer.STORAGE_STATES";

    private SharedPreferences preferences;
    private Context context;

    public PreferencesUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<SongUtil> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<SongUtil> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<SongUtil>>() {}.getType();

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
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();
    }


    /**
     * guardar valores para salvar as visualizações
     */


    public void StorePlayingState(boolean playingState) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("playingState", playingState);
        editor.apply();
    };

    public boolean GetPlayingState() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getBoolean("playingState", false);
    };

}
