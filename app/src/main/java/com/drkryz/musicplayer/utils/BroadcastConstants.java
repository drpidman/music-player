package com.drkryz.musicplayer.utils;

public class BroadcastConstants {

    private static final String packageName = "com.drkryz.musicplayer";

    public static final String RequestPrev = packageName + ".previousMedia";
    public static final String RequestSkip = packageName + ".skipMedia";
    public static final String RequestStop = packageName + ".stopMedia";
    public static final String RequestPlay = packageName + ".playMedia";
    public static final String RequestInit = packageName + ".initMedia";
    public static final String RequestPause = packageName + ".pauseMedia";
    public static final String RequestReset = packageName + ".resetMedia";
    public static final String RequestResume = packageName + ".resumeMedia";
    public static final String RequestRelease = packageName + ".release";
    public static final String RequestDestroy = packageName + ".destroy";

    public static final String Play = packageName + ".ui.play";
    public static final String Pause = packageName + ".ui.pause";


    public static final String RequestNotification = packageName + ".notification";
    public static final String RemoveNotification = packageName + ".notification.remove";
    public static final String UpdateMetaData = packageName + ".notification.update";



    public static final String ACTION_PLAY = packageName + ".ACTION_PLAY";
    public static final String ACTION_PAUSE = packageName + ".ACTION_PAUSE";
    public static final String ACTION_STOP = packageName + ".ACTION_STOP";
    public static final String ACTION_SKIP = packageName + ".ACTION_SKIP";
    public static final String ACTION_PREV = packageName + ".ACTION_PREVIOUS";
}
