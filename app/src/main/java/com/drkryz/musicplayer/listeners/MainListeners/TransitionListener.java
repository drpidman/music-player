package com.drkryz.musicplayer.listeners.MainListeners;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.custom.CustomImageView;

public class TransitionListener implements MotionLayout.TransitionListener {

    private LayoutInflater inflater = null;
    private final Activity context;


    private final ImageButton playbtn, prevbtn, nextbtn, drawer;
    private final SeekBar seekBar;
    private final TextView currentPlaying;
    private final ImageView album;

    public TransitionListener(Activity activity) {
        this.context = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        playbtn = context.findViewById(R.id.uiPlay);
        prevbtn = context.findViewById(R.id.uiPrevious);
        nextbtn = context.findViewById(R.id.uiSkip);
        drawer = context.findViewById(R.id.drawer);

        seekBar = context.findViewById(R.id.progressBar);
        album = context.findViewById(R.id.musicAlbum);

        currentPlaying = context.findViewById(R.id.currentSongTitle);
    }

    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {

        playbtn.setVisibility(View.INVISIBLE);
        prevbtn.setVisibility(View.INVISIBLE);
        nextbtn.setVisibility(View.INVISIBLE);

        seekBar.setVisibility(View.INVISIBLE);
        album.setVisibility(View.INVISIBLE);
        currentPlaying.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {
        
    }

    @Override
    public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {

        playbtn.setVisibility(View.VISIBLE);
        prevbtn.setVisibility(View.VISIBLE);
        nextbtn.setVisibility(View.VISIBLE);

        seekBar.setVisibility(View.VISIBLE);
        album.setVisibility(View.VISIBLE);

        currentPlaying.setVisibility(View.VISIBLE);

    }

    @Override
    public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {

    }
}
