package com.drkryz.musicplayer.Constants;

public class BroadcastConstants {

    private static final String packageName = "com.drkryz.musicplayer";
    // internal events
    public static final String PREV_CMD = packageName + ".previous";
    public static final String SKIP_CMD = packageName + ".skip";
    public static final String STOP_CMD = packageName + ".stop";
    public static final String PLAY_CMD = packageName + ".play";
    public static final String INIT_CMD = packageName + ".init";
    public static final String SEEK_CMD = packageName + ".seek";
    public static final String PAUSE_CMD = packageName + ".pause";
    public static final String RESET_CMD = packageName + ".reset";
    public static final String RESUME_CMD = packageName + ".resume";
    public static final String PREPARE_CMD = packageName + ".prepare";
    public static final String ON_RESUME_CMD = packageName + ".on.user.resume";
    // ui events with service
    public static final String UI_PLAY = packageName + ".ui.play";
    public static final String UI_PAUSE = packageName + ".ui.pause";
    public static final String UI_SKIP = packageName + ".ui.skip";
    public static final String UI_PREV = packageName + ".ui.previous";
    public static final String UI_RESUME = packageName + ".ui.resume";
    public static final String UI_UPDATE_MEDIA_CONTROL_BUTTON = packageName + ".ui.update_media_control_button";
    public static final String UI_UPDATE_MEDIA_PROGRESS = packageName + "ui.update_media_progress";
    public static final String UI_UPDATE_MEDIA_METADATA = packageName + "ui_update_media_metadata";
    // notification manager
    public static final String NOTIFICATION_CREATE = packageName + ".notification.create";
    public static final String NOTIFICATION_REMOVE = packageName + ".notification.remove";
    public static final String NOTIFICATION_UPDATE_METADATA = packageName + ".notification.update_metadata";
    // notification actions
    public static final String ACTION_PLAY = packageName + ".ACTION_PLAY";
    public static final String ACTION_PAUSE = packageName + ".ACTION_PAUSE";
    public static final String ACTION_STOP = packageName + ".ACTION_STOP";
    public static final String ACTION_SKIP = packageName + ".ACTION_SKIP";
    public static final String ACTION_PREV = packageName + ".ACTION_PREVIOUS";
    public static final String ACTION_CLOSE = packageName + ".ACTION_CLOSE";
}
