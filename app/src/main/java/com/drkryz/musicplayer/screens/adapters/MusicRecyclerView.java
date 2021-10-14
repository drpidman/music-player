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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.PlaybackAlbum;
import com.drkryz.musicplayer.utils.SongUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MusicRecyclerView extends RecyclerView.Adapter<MusicRecyclerView.ViewHolder> {

    private ArrayList<SongUtil> musicList;
    private Context context;

    @Override
    public MusicRecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View musicView = inflater.inflate(R.layout.listview_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(musicView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MusicRecyclerView.ViewHolder holder, int position) {
        SongUtil song = musicList.get(position);

        TextView musicTitle = holder.musicTitle;
        ImageView cover = holder.musicAlbumCover;


        cover.setImageBitmap(PlaybackAlbum.getCover(context, position, musicList));
        musicTitle.setText(song.getTitle());

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView musicTitle;
        public ImageView musicAlbumCover;

        public ViewHolder(View itemView) {
            super(itemView);

            musicTitle = itemView.findViewById(R.id.musicTitle);
            musicAlbumCover = itemView.findViewById(R.id.musicAlbumCover);
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }


    public MusicRecyclerView(ArrayList<SongUtil> music, Context ctx) {
        musicList = music;
        context = ctx;
    }
}
