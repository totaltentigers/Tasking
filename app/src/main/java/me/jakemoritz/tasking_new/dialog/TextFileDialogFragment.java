package me.jakemoritz.tasking_new.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TextFileDialogFragment extends DialogFragment {

    private String filename;

    public TextFileDialogFragment() {
    }

    public static TextFileDialogFragment newInstance(String filename){
        TextFileDialogFragment textFileDialogFragment = new TextFileDialogFragment();
        textFileDialogFragment.setRetainInstance(true);
        textFileDialogFragment.filename = filename;
        return textFileDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(readTextFile())
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        if (dialog != null && getRetainInstance()){
            dialog.setDismissMessage(null);
        }

        super.onDestroyView();
    }

    private String readTextFile() {
        StringBuilder text = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(getActivity().getAssets().open("license_" + filename.trim().toLowerCase() + ".txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return text.toString();
    }
}
