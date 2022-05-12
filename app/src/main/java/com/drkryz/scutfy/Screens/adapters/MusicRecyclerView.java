package com.drkryz.scutfy.Screens.adapters;

import android.content.Context;
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

public class MusicRecyclerView extends RecyclerView.Adapter<MusicRecyclerView.ViewHolder> implements Filterable {

    private final ArrayList<UserPlaylist> musicList;
    private final Context context;
    private final PreferencesUtil preferencesUtil;
    private final MusicService musicService;
    private ArrayList<UserPlaylist> musicListAll;

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

        musicListAll = new ArrayList<>(musicList);
        preferencesUtil = new PreferencesUtil(context);
    }
}