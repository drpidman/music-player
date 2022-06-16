package com.drkryz.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class Dialog extends DialogFragment {

    private final Context ctx;


    public Dialog(Context context) {
        this.ctx = context;
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle saved) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.ctx.getApplicationContext());

        return builder.create();
    }
}
