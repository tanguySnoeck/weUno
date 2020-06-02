package edu.vub.at.weuno;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class ContinueGameDialog extends DialogFragment {
    private boolean continues;
    private Activity activity;
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean continues() {
        return continues;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                continues = true;
                dismiss();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                continues = false;
                dismiss();
            }
        });

        return builder.create();
    }
}
