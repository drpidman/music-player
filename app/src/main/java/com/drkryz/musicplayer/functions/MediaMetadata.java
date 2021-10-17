package com.drkryz.musicplayer.functions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.utils.PreferencesUtil;
import com.drkryz.musicplayer.utils.SongUtil;

import java.io.IOException;
import java.util.ArrayList;

public class MediaMetadata {

    private static Bitmap cover = null;
    private static PreferencesUtil preferencesUtil;

    public static Bitmap getCover(Context context, int index, ArrayList<SongUtil> song) {
        preferencesUtil = new PreferencesUtil(context);

        SongUtil songUtil = song.get(index);
        String albumUri = songUtil.getAlbum();

        try {
            cover = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(),
                    Uri.parse(albumUri)
            );
        } catch (IOException e) {
            cover = BitmapFactory
                    .decodeResource(context.getResources(), R.drawable.default_music);
            e.printStackTrace();
        }

        return cover;
    }

    public static int getColor(Context context, Bitmap bitmap) {
        return DominantColor.GetDominantColor(bitmap);
    }
}
