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
    private final String STORAGE_USER = "COM.drkryz.musicplayer.STORAGE_USER";

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


    public void firstInit(boolean firstPlaying) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("FirstPlaying", firstPlaying);
        editor.apply();
    }

    public boolean GetFirstInit() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getBoolean("FirstPlaying", false);
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

    public void SetLastCover(String cover) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("LastCover", cover);
        editor.apply();
    }

    public void SetLastIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("lastIndex", index);
        editor.apply();
    }

    public int GetLastIndex() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return preferences.getInt("lastIndex", -1);
    }

    public void StoreCurrentTotalDuration(String total) {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("totalDuration", total);
        editor.apply();
    }

    public String LoadTotalDuration() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        return  preferences.getString("totalDuration", "0");
    }

    public void StoreUserInApp(boolean inactive) {
        preferences = context.getSharedPreferences(STORAGE_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("inApp", inactive);
        editor.apply();
    }

    public boolean LoadUserInApp() {
        preferences = context.getSharedPreferences(STORAGE_USER, Context.MODE_PRIVATE);
        return preferences.getBoolean("inApp", false);
    }

    public void clearCover() {
        preferences = context.getSharedPreferences(STORAGE_STATES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();
    }
}
