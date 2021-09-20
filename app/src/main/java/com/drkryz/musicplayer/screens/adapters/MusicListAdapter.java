package com.drkryz.musicplayer.screens.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.utils.Song;

import java.io.IOException;
import java.util.ArrayList;

public class MusicListAdapter extends BaseAdapter {

    private final ArrayList<Song> songs;
    private final Activity MusicList;

    LayoutInflater inflater = null;


    public MusicListAdapter(ArrayList<Song> songList, Activity activity) {
        this.songs = songList;
        this.MusicList = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return songs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertedView, ViewGroup viewGroup) {

        Song music = songs.get(i);

        if (convertedView == null) {
            convertedView = MusicList.getLayoutInflater().inflate(R.layout.listview_item, viewGroup, false);
        }

        TextView textTitle = convertedView.findViewById(R.id.musicTitle);
        textTitle.setText(music.getTitle());

        Bitmap cover = null;

        try {
            cover = MediaStore.Images.Media.getBitmap(
                    MusicList.getContentResolver(),
                    Uri.parse(music.getAlbum())
            );
        } catch (IOException e) {
            e.printStackTrace();
            cover = BitmapFactory.decodeResource(
                    MusicList.getResources(),
                    R.drawable.default_music
            );
        }

        ImageView coverImage = (ImageView) convertedView.findViewById(R.id.musicAlbumCover);
        coverImage.setImageBitmap(cover);

        return convertedView;
    }

}
