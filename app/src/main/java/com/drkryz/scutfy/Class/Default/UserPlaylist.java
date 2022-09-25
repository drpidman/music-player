package com.drkryz.scutfy.Class.Default;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.drkryz.scutfy.R;

import java.io.InputStream;

public class UserPlaylist {

    private String Title;
    private String Author;
    private String Path;
    private String Duration;
    private String Album;
    private boolean Favorite;

    /**
     *
     * @param title - music title
     * @param author - music author/artist
     * @param path - music path in storage
     * @param duration - music string duration(convert to int)
     * @param album - music album art
     */
    public UserPlaylist(String title, String author, String path, String duration, String album, boolean favorite) {
        Title = title;
        Author = author;
        Path = path;
        Duration = duration;
        Album = album;
        Favorite = favorite;
    }


    /**
     *
     * @return {@link #Title}
     */
    public String getTitle() {
        return Title;
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        Title = title;
    }

    /**
     *
     * @return {@link #Author}
     */
    public String getAuthor() {
        if (Author.equals("<unknown>")) {
            return "unknown";
        } else {
            return Author;
        }
    }

    /**
     *
     * @param author
     */
    public void setAuthor(String author) {
        Author = author;
    }

    /**
     *
     * @return {@link #Path}
     */
    public String getPath() {
        return Path;
    }

    /**
     *
     * @param path
     */
    public void setPath(String path) {
        Path = path;
    }

    /**
     *
     * @return {@link #Duration}
     */
    public String getDuration() {
        return Duration;
    }

    /**
     *
     * @param duration
     */
    public void setDuration(String duration) {
        Duration = duration;
    }

    /**
     *
     * @return {@link #Album}
     */
    public String getAlbum(Context ctx) {

        if (Album == null) {
            Album = ctx.getResources().getResourcePackageName(R.drawable.img_default_music);
        }

        return Album;
    }

    /**
     *
     * @param album
     */
    public void setAlbum(String album) {
        Album = album;
    }

    public void setFavorite(boolean isFav) {
        Favorite = isFav;
    }

    public boolean isFavorite() {
        return Favorite;
    }
}