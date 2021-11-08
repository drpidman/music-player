package com.drkryz.musicplayer.Class.Default;

public class UserFavorites {

    private String Title;
    private String Path;
    private String Author;
    private boolean Favorite;

    public UserFavorites(String title, String path, String author, boolean favorite) {
        Title = title;
        Path = path;
        Author = author;
        Favorite = favorite;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getPath() {
        return Path;
    }

    public void setPath(String path) {
        Path = path;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public boolean isFavorite() {
        return Favorite;
    }

    public void setFavorite(boolean favorite) {
        Favorite = favorite;
    }
}
