package me.jakemoritz.tasking.helper;

import com.google.api.client.util.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFormatter {

    private static DateFormatter dateFormatter;
    private SimpleDateFormat rfcFormatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

    private String[] suffixes =
            //    0     1     2     3     4     5     6     7     8     9
            { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    //    10    11    12    13    14    15    16    17    18    19
                    "th", "th", "th", "th", "th", "th", "th", "th", "th", "th",
                    //    20    21    22    23    24    25    26    27    28    29
                    "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th",
                    //    30    31
                    "th", "st" };

    public synchronized static DateFormatter getInstance(){
        if (dateFormatter == null){
            dateFormatter = new DateFormatter();
        }
        return dateFormatter;
    }

    // Build Calendar from fields
    public String formatDate(int dayOfMonth, int month, int year){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);

        return formatDate(calendar);
    }

    // Format string from Calendar object
    public String formatDate(Calendar calendar){
        int year = calendar.get(Calendar.YEAR);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        String dayString = dayOfMonth + suffixes[dayOfMonth];
        return dateFormat.format(calendar.getTime()) + " " + dayString + ", " + year;
    }

    // Build Calendar from DateTime
    public String formatDate(DateTime taskDateTime){
        Date parsedDate;
        try {
            parsedDate = rfcFormatter.parse(taskDateTime.toStringRfc3339().replace("Z", "").replace("T", "-"));

            // Create calendar from DateTime
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(parsedDate);

            return formatDate(taskCal);
        } catch (ParseException e){
            return "";
        }
    }
}
