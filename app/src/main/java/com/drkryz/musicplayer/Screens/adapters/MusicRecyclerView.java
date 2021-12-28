package com.drkryz.musicplayer.Screens.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.Utils.MediaMetadataUtil;
import com.drkryz.musicplayer.Services.MusicService;
import com.drkryz.musicplayer.Utils.PreferencesUtil;
import com.drkryz.musicplayer.Class.Default.UserPlaylist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MusicRecyclerView extends RecyclerView.Adapter<MusicRecyclerView.ViewHolder> {

    private final ArrayList<UserPlaylist> musicList;
    private final Context context;
    private final PreferencesUtil preferencesUtil;
    private final MusicService musicService;

    @NonNull
    @Override
    public MusicRecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View musicView = inflater.inflate(R.layout.listview_item, parent, false);

        return new ViewHolder(musicView);
    }

    @Override
    public void onBindViewHolder(MusicRecyclerView.ViewHolder holder, int position) {
        UserPlaylist song = musicList.get(position);

        TextView musicTitle = holder.musicTitle;
        TextView musicAuthor = holder.musicAuthor;
        ImageView cover = holder.musicAlbumCover;

        cover.setImageBitmap(MediaMetadataUtil.getCover(context, position, musicList));
        musicTitle.setText(song.getTitle());
        musicAuthor.setText(song.getAuthor());


    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

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


    public MusicRecyclerView(ArrayList<UserPlaylist> music, Context ctx) {
        this.musicList = music;
        this.context = ctx;
        this.musicService = null;

        preferencesUtil = new PreferencesUtil(context);
    }

    public MusicRecyclerView(ArrayList<UserPlaylist> music, Context ctx, MusicService musicService) {
        this.musicList = music;
        this.context = ctx;
        this.musicService = musicService;

        preferencesUtil = new PreferencesUtil(context);
    }
}
