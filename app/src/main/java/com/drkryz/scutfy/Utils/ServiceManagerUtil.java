package com.drkryz.scutfy.Utils;

import android.content.Context;
import android.content.Intent;

import com.drkryz.scutfy.Constants.BroadcastConstants;
import com.drkryz.scutfy.Services.MusicService;

public class ServiceManagerUtil {

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
            case 8:
                service.setAction(BroadcastConstants.FAVORITE_CMD);
                context.startService(service);
                break;
            case 9:
                service.setAction(BroadcastConstants.SHUFFLE_CMD);
                context.startService(service);
                break;
            case 10:
                service.setAction(BroadcastConstants.LOOPING_CMD);
                context.startService(service);
                break;
        }
    }
}
