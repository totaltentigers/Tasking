package me.jakemoritz.tasking;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment{

    private static final String TAG = "DatePickerFragment";

    private Fragment fragment;

    public static DatePickerFragment newInstance(Fragment parentFragment){
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.fragment = parentFragment;
        return datePickerFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use current date as default date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        if (fragment instanceof  AddTaskDialogFragment){
            return new DatePickerDialog(getActivity(),(AddTaskDialogFragment) fragment, year, month, day);
        } else {
            return new DatePickerDialog(getActivity(),(EditTaskDialogFragment) fragment, year, month, day);
        }
    }
}
