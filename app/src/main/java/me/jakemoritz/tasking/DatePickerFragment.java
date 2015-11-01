package me.jakemoritz.tasking;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment{

    private AddTaskDialogFragment addTaskDialogFragment;

    public DatePickerFragment(AddTaskDialogFragment addTaskDialogFragment) {
        super();
        this.addTaskDialogFragment = addTaskDialogFragment;
    }

    private static final String TAG = "DatePickerFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use current date as default date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), addTaskDialogFragment, year, month, day);
    }
}
