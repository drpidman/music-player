

package com.drkryz.scutfy.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Picture;
import android.net.Uri;
import android.provider.MediaStore;

import com.drkryz.scutfy.R;
import com.drkryz.scutfy.Class.Default.UserPlaylist;

import java.io.IOException;
import java.util.ArrayList;

public class MediaMetadataUtil {

    public static Bitmap getCover(Context context, int index, ArrayList<UserPlaylist> song) {
        UserPlaylist userPlaylist = song.get(index);
        Uri uri = Uri.parse(userPlaylist.getAlbum(context));

        Bitmap cover = null;

        try {
            cover = MediaStore.Images.Media.getBitmap(
                    context.getContentResolver(),
                    uri
            );
        } catch (IOException e) {
            cover = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder_ic);
        }


        return cover;
    }

    public static int getDominantColor(Bitmap bitmap) {
        if (bitmap == null) return Color.TRANSPARENT;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int[] pixels = new int[size];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int color;
        int r = 0;
        int g = 0;
        int b = 0;
        int a;

        int count = 0;

        for (int pixel : pixels) {
            color = pixel;
            a = Color.alpha(color);
            if (a > 0) {
                r += Color.red(color);
                g += Color.green(color);
                b += Color.blue(color);
                count++;
            }
        }

        r /= count;
        g /= count;
        b /= count;
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = b & 0x000000FF;
        color = 0xFF000000 | r | g | b;

        return color;
    }
}