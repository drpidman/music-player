package com.drkryz.scutfy.Class.Default;

import android.content.Context;

import com.drkryz.scutfy.R;

public class UserPlaylist {

    private String Title;
    private String Author;
    private String Path;
    private String Duration;
    private String Album;
    private String Mime_Type;
    private boolean Favorite;

    /**
     *  @param title - music title
     * @param author - music author/artist
     * @param path - music path in storage
     * @param duration - music string duration(convert to int)
     * @param album - music album art
     * @param mime_type - music file type
     */
    public UserPlaylist(String title, String author, String path, String duration, String album, boolean favorite, String mime_type) {
        Title = title;
        Author = author;
        Path = path;
        Duration = duration;
        Album = album;
        Favorite = favorite;
        Mime_Type = mime_type;
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

    public String getMime_Type() {
        return Mime_Type;
    }

    public void setMime_Type(String mime_Type) {
        Mime_Type = mime_Type;
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