package com.drkryz.musicplayer.listeners.MainListeners;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.drkryz.musicplayer.R;
import com.drkryz.musicplayer.custom.CustomImageView;

public class TransitionListener implements MotionLayout.TransitionListener {

    private LayoutInflater inflater = null;
    private Activity context;

    public TransitionListener(Activity activity) {
        this.context = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int startId, int endId) {

    }

    @Override
    public void onTransitionChange(MotionLayout motionLayout, int startId, int endId, float progress) {

    }

    @Override
    public void onTransitionCompleted(MotionLayout motionLayout, int currentId) {
        TextView playerWindow = context.findViewById(R.id.textTitle);
        ImageButton drawer = context.findViewById(R.id.drawer);
        RecyclerView listView = context.findViewById(R.id.musicList);
        CustomImageView imageView = context.findViewById(R.id.musicAlbum);

        ConstraintSet currentConstraint =  motionLayout.getConstraintSet(currentId);
        ConstraintSet constraintEnd = motionLayout.getConstraintSet(R.id.end);
        ConstraintSet constraintStart = motionLayout.getConstraintSet(R.id.start);


        if (constraintStart == currentConstraint) {
            playerWindow.setText("PLAYER");
            imageView.setVisibility(View.VISIBLE);
        } else if (constraintEnd == currentConstraint) {
            playerWindow.setText("MUSICAS");
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {

    }
}
