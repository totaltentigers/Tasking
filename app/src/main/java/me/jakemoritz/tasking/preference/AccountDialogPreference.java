package me.jakemoritz.tasking.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import me.jakemoritz.tasking.activity.MainActivity;
import me.jakemoritz.tasking.misc.App;

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
            // Send intent to MainActivity to start sign-out procedure
            Intent clickIntent = new Intent(App.getInstance(), MainActivity.class);
            clickIntent.putExtra("signOut", true);
            clickIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            App.getInstance().startActivity(clickIntent);
        }
        else {
            dialog.cancel();
        }
    }

}
