package me.jakemoritz.tasking;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    static String[] suffixes =
            //    0     1     2     3     4     5     6     7     8     9
            { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    //    10    11    12    13    14    15    16    17    18    19
                    "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                    //    20    21    22    23    24    25    26    27    28    29
                    "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    //    30    31
                    "th", "st" };

    public static String formatDate(int year, int monthOfYear, int dayOfMonth){
        Date date = new Date(year, monthOfYear, dayOfMonth);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM");

        String dayString = dayOfMonth + suffixes[dayOfMonth];
        return dateFormat.format(date) + " " + dayString + ", " + year;
    }
}
