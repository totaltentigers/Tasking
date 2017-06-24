package me.jakemoritz.tasking_new.helper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import me.jakemoritz.tasking_new.R;
import me.jakemoritz.tasking_new.dialog.PermissionRationaleDialogFragment;

public class PermissionHelper {

    private static PermissionHelper permissionHelper;

    public final static int GET_ACCOUNTS_REQ_CODE = 1111;

    public synchronized static PermissionHelper getInstance(){
        if (permissionHelper == null){
            permissionHelper = new PermissionHelper();
        }
        return permissionHelper;
    }

    public boolean permissionGranted(Activity activity, String permission){
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(Activity activity, String permission){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            // show alert dialog async

            PermissionRationaleDialogFragment permissionRationaleDialogFragment = PermissionRationaleDialogFragment.newInstance(activity, Manifest.permission.GET_ACCOUNTS, activity.getString(R.string.get_accounts_permission_denied));
            permissionRationaleDialogFragment.show(activity.getFragmentManager(), PermissionRationaleDialogFragment.class.getSimpleName());
        } else {
            requestPermissionDirect(activity, permission);
        }
    }

    public void requestPermissionDirect(Activity activity, String permission){
        int requestCode = -1;

        switch (permission){
            case (Manifest.permission.GET_ACCOUNTS):
                requestCode = GET_ACCOUNTS_REQ_CODE;
                break;
        }

        if (requestCode >= 0){
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

}
