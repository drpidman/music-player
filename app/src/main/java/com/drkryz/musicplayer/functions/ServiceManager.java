package com.drkryz.musicplayer.functions;

import android.content.Context;
import android.content.Intent;

import com.drkryz.musicplayer.constants.BroadcastConstants;
import com.drkryz.musicplayer.services.MusicService;

public class ServiceManager {

    public static void handleAction(int action, Context context) {
        Intent service = new Intent(context, MusicService.class);

        switch (action) {
            case 0:
                service.setAction(BroadcastConstants.PREPARE_CMD);
                context.startService(service);
                break;
            case 1:
                service.setAction(BroadcastConstants.INIT_CMD);
                context.startService(service);
                break;
            case 2:
                service.setAction(BroadcastConstants.PLAY_CMD);
                context.startService(service);
                break;
            case 3:
                service.setAction(BroadcastConstants.PAUSE_CMD);
                context.startService(service);
                break;
            case 4:
                service.setAction(BroadcastConstants.RESUME_CMD);
                context.startService(service);
                break;
            case 5:
                service.setAction(BroadcastConstants.SKIP_CMD);
                context.startService(service);
                break;
            case 6:
                service.setAction(BroadcastConstants.PREV_CMD);
                context.startService(service);
                break;
            case 7:
                service.setAction(BroadcastConstants.ON_RESUME_CMD);
                context.startService(service);
                break;
        }
    }
}
