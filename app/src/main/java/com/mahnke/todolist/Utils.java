package com.mahnke.todolist;

import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Utils {
    public static final SimpleDateFormat ISO_8601 =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final DateFormat SHORT_DATE = DateFormat.getDateInstance(DateFormat.SHORT);
    public static final DateFormat SHORT_TIME = DateFormat.getTimeInstance(DateFormat.SHORT);

    private Utils() {
    }

    public static Locale getPreferredLocale(Context context) {
        // TODO update for API level 24
        return context.getResources().getConfiguration().locale;
    }

    public static String getSqlFormattedDateTime(Date date) {
        return ISO_8601.format(date);
    }

    public static String getSqlFormattedDateTime(long millis) {
        return getSqlFormattedDateTime(new Date(millis));
    }

    public static String getSqlFormattedDateTime(Calendar calendar) {
        return getSqlFormattedDateTime(calendar.getTime());
    }

    public static Calendar getCalendarFromMillis(long millis) {
        Calendar retVal = Calendar.getInstance();
        retVal.setTimeInMillis(millis);
        return retVal;
    }

    public static Calendar getCalendarFromIso8601(String iso8601) throws ParseException {
        return getCalendarFromMillis(ISO_8601.parse(iso8601).getTime());
    }

    public static String getPrettyDate(Calendar date) {
        return SHORT_DATE.format(date.getTime());
    }

    public static String getPrettyTime(Calendar time) {
        return SHORT_TIME.format(time.getTime());
    }
}
