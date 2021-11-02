package com.drkryz.musicplayer.functions;

import android.graphics.Bitmap;
import android.graphics.Color;

public class DominantColor {
    public static int GetDominantColor(Bitmap bitmap) {
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