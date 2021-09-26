package com.drkryz.musicplayer.utils;

import static com.drkryz.musicplayer.constants.BroadcastConstants.RemoveAudioFocus;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RemoveNotification;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestAudioFocus;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestDestroy;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestInit;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestNotification;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestPause;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestPlay;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestPrev;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestRelease;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestReset;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestResume;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestSeek;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestSkip;
import static com.drkryz.musicplayer.constants.BroadcastConstants.RequestStop;
import static com.drkryz.musicplayer.constants.BroadcastConstants.UpdateMetaData;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.drkryz.musicplayer.constants.BroadcastConstants;

public class BroadcastUtils {

    private final GlobalsUtil globalsUtil;

    public BroadcastUtils(Context context) {
        globalsUtil = (GlobalsUtil) context.getApplicationContext();
    }

    public void playbackManager(String action, long seekVal) {
        Intent intent;

        switch (action) {
            case RequestInit:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestInit);
                globalsUtil.sendBroadcast(intent);
                break;
            case RequestPlay:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestPlay);
                globalsUtil.sendBroadcast(intent);
                break;
            case RequestPause:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestPause);
                globalsUtil.sendBroadcast(intent);
                break;
            case RequestSkip:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestSkip);
                globalsUtil.sendBroadcast(intent);
                break;
            case RequestPrev:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestPrev);
                globalsUtil.sendBroadcast(intent);
            case RequestStop:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestStop);
                globalsUtil.sendBroadcast(intent);
                break;
            case RequestResume:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestResume);
                globalsUtil.sendBroadcast(intent);
                break;
            case RequestSeek:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestSeek);
                intent.putExtra("seekTo", seekVal);
                globalsUtil.sendBroadcast(intent);
            case RequestReset:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestReset);
                globalsUtil.sendBroadcast(intent);
                break;
            case RequestRelease:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestRelease);
                globalsUtil.sendBroadcast(intent);
            case RequestDestroy:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestDestroy);
                globalsUtil.sendBroadcast(intent);
                break;
            case RequestAudioFocus:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestAudioFocus);
                globalsUtil.sendBroadcast(intent);
            break;
            case RemoveAudioFocus:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RemoveAudioFocus);
                globalsUtil.sendBroadcast(intent);
            break;
        }
    }

    public void playbackUIManager(String action, boolean isPlaying) {
        Intent intent;
        switch (action) {
            case BroadcastConstants.Play:
                intent = new Intent(BroadcastConstants.Play);
                globalsUtil.sendBroadcast(intent);
                break;
            case BroadcastConstants.Pause:
                intent = new Intent(BroadcastConstants.Pause);
                globalsUtil.sendBroadcast(intent);
                break;
            case BroadcastConstants.Resume:
                intent = new Intent(BroadcastConstants.Resume);
                globalsUtil.sendBroadcast(intent);
                break;
            case BroadcastConstants.Skip:
                intent = new Intent(BroadcastConstants.Skip);
                globalsUtil.sendBroadcast(intent);
                break;
            case BroadcastConstants.Prev:
                intent = new Intent(BroadcastConstants.Prev);
                globalsUtil.sendBroadcast(intent);
            case BroadcastConstants.RequestPlayChange:
                intent = new Intent(BroadcastConstants.RequestPlayChange);
                intent.putExtra("playingState", isPlaying);
                globalsUtil.sendBroadcast(intent);
                break;
            case BroadcastConstants.UpdateCover:
                intent = new Intent(BroadcastConstants.UpdateCover);
                globalsUtil.sendBroadcast(intent);
                break;
            case BroadcastConstants.RequestProgress:
                intent = new Intent(BroadcastConstants.RequestProgress);
                globalsUtil.sendBroadcast(intent);
                break;
        }
    }

    public IntentFilter playbackUIFilter(String action) {
        switch (action) {
            case BroadcastConstants.Play:
                Log.d(action, "");
                return new IntentFilter(BroadcastConstants.Play);
            case BroadcastConstants.Pause:
                return new IntentFilter(BroadcastConstants.Pause);
            case BroadcastConstants.Skip:
                return new IntentFilter(BroadcastConstants.Skip);
            case BroadcastConstants.Prev:
                return new IntentFilter(BroadcastConstants.Prev);
            case BroadcastConstants.Resume:
                Log.d(action, "");
                return new IntentFilter(BroadcastConstants.Resume);
            case BroadcastConstants.RequestPlayChange:
                Log.d(action, "");
                return new IntentFilter(BroadcastConstants.RequestPlayChange);
            case BroadcastConstants.UpdateCover:
                Log.d(action, "");
                return new IntentFilter(BroadcastConstants.UpdateCover);
            case BroadcastConstants.RequestProgress:
                Log.d(action, "");
                return new IntentFilter(BroadcastConstants.RequestProgress);
        }
        return null;
    }


    public IntentFilter playbackFilter(String action) {
        switch (action) {
            case RequestInit:
                Log.d(action, "");
                return new IntentFilter(RequestInit);
            case RequestPlay:
                Log.d(action, "");
                return new IntentFilter(RequestPlay);
            case RequestPause:
                Log.d(action, "");
                return new IntentFilter(RequestPause);
            case RequestSkip:
                Log.d(action, "");
                return new IntentFilter(RequestSkip);
            case RequestPrev:
                Log.d(action, "");
                return new IntentFilter(RequestPrev);
            case RequestStop:
                Log.d(action, "");
                return new IntentFilter(RequestStop);
            case RequestSeek:
                Log.d(action, "");
                return new IntentFilter(RequestSeek);
            case RequestResume:
                Log.d(action, "");
                return new IntentFilter(RequestResume);
            case RequestReset:
                Log.d(action, "");
                return new IntentFilter(RequestReset);
            case RequestRelease:
                Log.d(action, "");
                return new IntentFilter(RequestRelease);
            case RequestDestroy:
                Log.d(action, "");
                return new IntentFilter(RequestDestroy);
            case RequestNotification:
                Log.d(action, "");
                return new IntentFilter(RequestNotification);
            case RemoveNotification:
                Log.d(action, "");
                return new IntentFilter(RemoveNotification);
            case UpdateMetaData:
                Log.d(action, "");
                return new IntentFilter(UpdateMetaData);
            case RequestAudioFocus:
                Log.d(action, "");
                return new IntentFilter(RequestAudioFocus);
            case RemoveAudioFocus:
                Log.d(action, "");
                return new IntentFilter(RemoveAudioFocus);
        }
        return null;
    }

    public void playbackNotification(String action, GlobalsUtil.Status status) {
        Intent intent;
        switch (action) {
            case RequestNotification:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RequestNotification);
                intent.putExtra("status", status);
                globalsUtil.sendBroadcast(intent);
                break;
            case RemoveNotification:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(RemoveNotification);
                globalsUtil.sendBroadcast(intent);
                break;
            case UpdateMetaData:
                Log.d(globalsUtil.getPackageName(), action);
                intent = new Intent(UpdateMetaData);
                globalsUtil.sendBroadcast(intent);
                break;
        }
    }
}