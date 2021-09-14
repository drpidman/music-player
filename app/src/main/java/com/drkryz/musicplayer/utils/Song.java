package com.drkryz.musicplayer.utils;

public class Song {

    private String Title;
    private String Author;
    private String Path;
    private String Duration;
    private String Album;

    public Song(String title, String author, String path, String duration, String album) {
        Title = title;
        Author = author;
        Path = path;
        Duration = duration;
        Album = album;
    }


    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getPath() {
        return Path;
    }

    public void setPath(String path) {
        Path = path;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public String getAlbum() {
        return Album;
    }

    public void setAlbum(String album) {
        Album = album;
    }
}