package me.jakemoritz.tasking.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment{

    private static final String TAG = "DatePickerDialogFragment";

    private Fragment fragment;

    public static DatePickerDialogFragment newInstance(Fragment parentFragment){
        DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
        datePickerDialogFragment.fragment = parentFragment;
        return datePickerDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use current date as default date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (fragment instanceof AddTaskDialogFragment){
            return new DatePickerDialog(getActivity(),(AddTaskDialogFragment) fragment, year, month, day);
        } else {
            return new DatePickerDialog(getActivity(),(EditTaskDialogFragment) fragment, year, month, day);
        }
    }
}
