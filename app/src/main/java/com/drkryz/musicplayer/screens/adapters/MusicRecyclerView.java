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
import com.drkryz.musicplayer.utils.Song;

import java.io.IOException;
import java.util.ArrayList;

public class MusicRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<Song> songs;
    private final Activity MusicList;


    public MusicRecyclerView(ArrayList<Song> songList, Activity activity) {
        this.songs = songList;
        this.MusicList = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View musicListView = inflater.inflate(R.layout.listview_item, parent, false);

        RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(musicListView) {
            @Override
            public String toString() {
                return super.toString();
            }
        };

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song musicIndex = songs.get(position);

        TextView textTitle = holder.itemView.findViewById(R.id.musicTitle);
        textTitle.setText(musicIndex.getTitle());

        Bitmap cover = null;

        try {
            cover = MediaStore.Images.Media.getBitmap(
                    MusicList.getContentResolver(),
                    Uri.parse(musicIndex.getAlbum())
            );
        } catch (IOException e) {
            e.printStackTrace();
            cover = BitmapFactory.decodeResource(
                    MusicList.getResources(),
                    R.drawable.default_music
            );
        }



        ImageView coverImage = (ImageView) holder.itemView.findViewById(R.id.musicAlbumCover);
        coverImage.setImageBitmap(cover);

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
