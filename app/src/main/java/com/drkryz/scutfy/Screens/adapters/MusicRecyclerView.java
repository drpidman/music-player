package com.drkryz.scutfy.Screens.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drkryz.scutfy.R;
import com.drkryz.scutfy.Utils.MediaMetadataUtil;
import com.drkryz.scutfy.Services.MusicService;
import com.drkryz.scutfy.Utils.PreferencesUtil;
import com.drkryz.scutfy.Class.Default.UserPlaylist;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MusicRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final ArrayList<UserPlaylist> musicList;
    private final Context context;
    private final PreferencesUtil preferencesUtil;
    private final MusicService musicService;
    private ArrayList<UserPlaylist> musicListAll;

    private int HEADER_VIEW = 0;
    private int MUSIC_LIST = 1;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View vw;
        RecyclerView.ViewHolder vh;

        if (viewType == HEADER_VIEW) {
            vw = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_header, parent, false);
            vh = new HeaderViewHolder(vw);
        } else {

            vw = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_item, parent, false);
            vh = new MusicListViewHolder(vw);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MusicListViewHolder) {
            MusicListViewHolder musicListHolder = (MusicListViewHolder) holder;

            UserPlaylist song = getItem(position);



            TextView musicTitle = musicListHolder.musicTitle;
            TextView musicAuthor = musicListHolder.musicAuthor;
            ImageView cover = musicListHolder.musicAlbumCover;


            cover.setImageBitmap(MediaMetadataUtil.getCover(context, position -1, musicList));

            musicTitle.setText(song.getTitle());
            musicAuthor.setText(song.getAuthor());

        } else if (holder instanceof HeaderViewHolder) {

        }
    }

    

    @Override
    public Filter getFilter() {
        return filter;
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<UserPlaylist> filtered = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0 ) {
                filtered.addAll(musicListAll);
            } else {
                String filter = charSequence.toString().toLowerCase().trim();

                for (UserPlaylist usr: musicListAll) {
                    if (usr.getTitle().toLowerCase().contains(filter)) {
                        filtered.add(usr);
                    }
                }
            }

            FilterResults res = new FilterResults();
            res.values = filtered;
            res.count = filtered.size();
            return res;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            musicList.clear();
            musicList.addAll((ArrayList) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public static class MusicListViewHolder extends RecyclerView.ViewHolder {

        public TextView musicTitle, musicAuthor;
        public ImageView musicAlbumCover;
        public androidx.constraintlayout.widget.ConstraintLayout constraintLayout;

        public MusicListViewHolder(View itemView) {
            super(itemView);

            musicTitle = itemView.findViewById(R.id.musicTitle);
            musicAuthor = itemView.findViewById(R.id.musicAuthor);
            musicAlbumCover = itemView.findViewById(R.id.musicAlbumCover);
            constraintLayout = itemView.findViewById(R.id.itemSpace);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size() + 1;
    }

    public UserPlaylist getItem(int pos) {
        return musicList.get(pos - 1);
    }


    @Override
    public int getItemViewType(int position) {
         if (position == 0) {
            return HEADER_VIEW;
        } else {
             return MUSIC_LIST;
         }
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

        musicListAll = new ArrayList<>(musicList);
        preferencesUtil = new PreferencesUtil(context);
    }
}