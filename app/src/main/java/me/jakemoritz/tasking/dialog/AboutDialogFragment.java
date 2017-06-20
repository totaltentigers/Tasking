package me.jakemoritz.tasking.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import me.jakemoritz.tasking.R;

public class AboutDialogFragment extends DialogFragment {

    private final static String APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0";

    public AboutDialogFragment() {
    }

    public static AboutDialogFragment newInstance(){
        AboutDialogFragment aboutDialogFragment = new AboutDialogFragment();
        aboutDialogFragment.setRetainInstance(true);
        return aboutDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_about, null);
        View titleView = getActivity().getLayoutInflater().inflate(R.layout.dialog_about_title, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("About")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setCustomTitle(titleView)
                .setView(dialogView);

        Uri apacheLicenseUri = Uri.parse(APACHE_LICENSE_URL);
        final Intent apacheLicenseIntent = new Intent(Intent.ACTION_VIEW, apacheLicenseUri);

        if (apacheLicenseIntent.resolveActivity(getActivity().getPackageManager()) != null){
            TextView retrofitLicenseWeb = (TextView) dialogView.findViewById(R.id.about_retrofit_web);
            retrofitLicenseWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(apacheLicenseIntent);
                }
            });
        } else {
            TextView retrofitLocal = (TextView) dialogView.findViewById(R.id.about_retrofit_local);
        }

        return builder.create();
    }
}
