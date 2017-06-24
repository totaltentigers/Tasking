package me.jakemoritz.tasking_new.dialog;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import me.jakemoritz.tasking_new.helper.PermissionHelper;

public class PermissionRationaleDialogFragment extends DialogFragment {

    private Activity activity;
    private String dialogMessage;
    private String permission;

    public static PermissionRationaleDialogFragment newInstance(Activity activity, String permission, String message){
        PermissionRationaleDialogFragment permissionRationaleDialogFragment = new PermissionRationaleDialogFragment();
        permissionRationaleDialogFragment.setRetainInstance(true);
        permissionRationaleDialogFragment.dialogMessage = message;
        permissionRationaleDialogFragment.activity = activity;
        permissionRationaleDialogFragment.permission = permission;
        return permissionRationaleDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(dialogMessage)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        switch (permission){
            case (Manifest.permission.GET_ACCOUNTS):
                PermissionHelper.getInstance().requestPermissionDirect(activity, permission);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        if (dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

}
