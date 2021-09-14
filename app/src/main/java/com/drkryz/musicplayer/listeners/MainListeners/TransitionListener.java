package com.drkryz.musicplayer.listeners.MainListeners;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.drkryz.musicplayer.R;

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

        ConstraintSet currentConstraint =  motionLayout.getConstraintSet(currentId);
        ConstraintSet constraintEnd = motionLayout.getConstraintSet(R.id.end);
        ConstraintSet constraintStart = motionLayout.getConstraintSet(R.id.start);


        if (constraintStart == currentConstraint) {
            playerWindow.setText("PLAYER");
            drawer.animate().rotation(360);
        } else if (constraintEnd == currentConstraint) {
            playerWindow.setText("MUSICAS");
            drawer.animate().rotation(180);
        }
    }

    @Override
    public void onTransitionTrigger(MotionLayout motionLayout, int triggerId, boolean positive, float progress) {

    }
}
