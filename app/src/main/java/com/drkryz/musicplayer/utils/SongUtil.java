package com.drkryz.musicplayer.utils;

import android.net.Uri;

public class SongUtil {

    private String Title;
    private String Author;
    private String Path;
    private String Duration;
    private String Album;

    /**
     * 
     * @param title - music title
     * @param author - music author/artist
     * @param path - music path in storage
     * @param duration - music string duration(convert to int)
     * @param album - music album art
     */
    public SongUtil(String title, String author, String path, String duration, String album) {
        Title = title;
        Author = author;
        Path = path;
        Duration = duration;
        Album = album;
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
        return Author;
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
    public String getAlbum() {
        return Album;
    }

    /**
     * 
     * @param album
     */
    public void setAlbum(String album) {
        Album = album;
    }
}