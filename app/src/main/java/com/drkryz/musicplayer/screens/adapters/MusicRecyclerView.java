package com.drkryz.musicplayer.screens.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.functions.MediaMetadata;
import com.drkryz.musicplayer.utils.SongUtil;

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
        TextView musicAuthor = holder.musicAuthor;
        ImageView cover = holder.musicAlbumCover;


        cover.setImageBitmap(MediaMetadata.getCover(context, position, musicList));
        musicTitle.setText(song.getTitle());
        musicAuthor.setText(song.getAuthor());

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView musicTitle, musicAuthor;
        public ImageView musicAlbumCover;
        public androidx.constraintlayout.widget.ConstraintLayout constraintLayout;

        public ViewHolder(View itemView) {
            super(itemView);



            musicTitle = itemView.findViewById(R.id.musicTitle);
            musicAuthor = itemView.findViewById(R.id.musicAuthor);
            musicAlbumCover = itemView.findViewById(R.id.musicAlbumCover);
            constraintLayout = itemView.findViewById(R.id.itemSpace);
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
