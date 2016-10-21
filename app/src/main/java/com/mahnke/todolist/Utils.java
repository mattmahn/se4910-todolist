package com.mahnke.todolist;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Utils {
    public static final SimpleDateFormat ISO_8601 =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

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
}
