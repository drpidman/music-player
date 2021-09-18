package com.drkryz.musicplayer.utils;

import static com.drkryz.musicplayer.utils.BroadcastConstants.RemoveNotification;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestDestroy;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestInit;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestNotification;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestPause;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestPlay;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestPrev;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestRelease;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestReset;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestResume;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestSkip;
import static com.drkryz.musicplayer.utils.BroadcastConstants.RequestStop;
import static com.drkryz.musicplayer.utils.BroadcastConstants.UpdateMetaData;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.PlaybackState;
import android.util.Log;

public class BroadcastSenders {

    private final GlobalVariables globalVariables;

    public BroadcastSenders(Context context) {
        globalVariables = (GlobalVariables) context.getApplicationContext();
    }

    public Intent playbackManager(String action) {
        Intent intent;

        switch (action) {
            case RequestInit:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestInit);
                globalVariables.sendBroadcast(intent);
            break;
            case RequestPlay:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestPlay);
                globalVariables.sendBroadcast(intent);
            break;
            case RequestPause:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestPause);
                globalVariables.sendBroadcast(intent);
            break;
            case RequestSkip:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestSkip);
                globalVariables.sendBroadcast(intent);
            break;
            case RequestPrev:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestPrev);
                globalVariables.sendBroadcast(intent);
            case RequestStop:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestStop);
                globalVariables.sendBroadcast(intent);
            break;
            case RequestResume:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestResume);
                globalVariables.sendBroadcast(intent);
            break;
            case RequestReset:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestReset);
                globalVariables.sendBroadcast(intent);
            break;
            case RequestRelease:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestRelease);
                globalVariables.sendBroadcast(intent);
            case RequestDestroy:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestDestroy);
                globalVariables.sendBroadcast(intent);
            break;
        }
        return null;
    }

    public Intent playbackUIManager(String action) {
        Intent intent;
        switch (action) {
            case BroadcastConstants.Play:
                intent = new Intent(BroadcastConstants.Play);
                globalVariables.sendBroadcast(intent);
            break;
            case BroadcastConstants.Pause:
                intent = new Intent(BroadcastConstants.Pause);
                globalVariables.sendBroadcast(intent);
            break;
            case BroadcastConstants.Resume:
                intent = new Intent(BroadcastConstants.Resume);
                globalVariables.sendBroadcast(intent);
            break;
        }
        return null;
    }

    public IntentFilter playbackUIFilter(String action) {
        switch (action) {
            case BroadcastConstants.Play:
                Log.d(action, "");
                return new IntentFilter(BroadcastConstants.Play);
            case BroadcastConstants.Pause:
                return new IntentFilter(BroadcastConstants.Pause);
            case BroadcastConstants.Resume:
                Log.d(action, "");
                return new IntentFilter(BroadcastConstants.Resume);
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
        }
        return null;
    }

    public Intent playbackNotification(String action, GlobalVariables.Status status) {
        Intent intent;
        switch (action) {
            case RequestNotification:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RequestNotification);
                intent.putExtra("status", status);
                globalVariables.sendBroadcast(intent);
            break;
            case RemoveNotification:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(RemoveNotification);
                globalVariables.sendBroadcast(intent);
            break;
            case UpdateMetaData:
                Log.d(globalVariables.getPackageName(), action);
                intent = new Intent(UpdateMetaData);
                globalVariables.sendBroadcast(intent);
            break;
        }
        return null;
    }
}
