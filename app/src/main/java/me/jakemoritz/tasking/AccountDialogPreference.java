package me.jakemoritz.tasking;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class AccountDialogPreference extends DialogPreference {

    private Context context;

    public AccountDialogPreference(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        this.context = context;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        if (which == Dialog.BUTTON_POSITIVE){
            ((MainActivity) context).signOutHelper();
        }
        else {
            dialog.cancel();
        }
    }

}
