package me.jakemoritz.tasking;

import java.sql.Time;
import java.text.SimpleDateFormat;

/**
 * Created by jakem on 11/2/2015.
 */
public class TimeFormatter {

    public static String formatTime(int hourOfDay, int minute){
        Time time = new Time(hourOfDay, minute, 0);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

        String timeString = timeFormat.format(time);

        return timeString;
    }

}
