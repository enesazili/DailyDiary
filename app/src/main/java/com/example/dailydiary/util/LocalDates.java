package com.example.dailydiary.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class LocalDates {

    public static final String PATTERN = "yyyy-MM-dd";

    private LocalDates() {
    }

    public static String today() {
        return format(Calendar.getInstance());
    }

    public static String yesterday() {
        return plusDays(today(), -1);
    }

    public static String plusDays(String localDate, int days) {
        Calendar calendar = parse(localDate);
        if (calendar == null) {
            return localDate;
        }
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return format(calendar);
    }

    public static String format(Calendar calendar) {
        SimpleDateFormat format = usFormat();
        return format.format(calendar.getTime());
    }

    public static Calendar parse(String localDate) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(usFormat().parse(localDate));
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        } catch (ParseException | RuntimeException exception) {
            return null;
        }
    }

    public static String displayLong(String localDate) {
        Calendar calendar = parse(localDate);
        if (calendar == null) {
            return localDate;
        }
        SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy EEEE", new Locale("tr", "TR"));
        format.setTimeZone(TimeZone.getDefault());
        return format.format(calendar.getTime());
    }

    public static String displayMedium(String localDate) {
        Calendar calendar = parse(localDate);
        if (calendar == null) {
            return localDate;
        }
        SimpleDateFormat format = new SimpleDateFormat("d MMMM EEEE", new Locale("tr", "TR"));
        format.setTimeZone(TimeZone.getDefault());
        return format.format(calendar.getTime());
    }

    public static String monthTitle(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat format = new SimpleDateFormat("LLLL yyyy", new Locale("tr", "TR"));
        format.setTimeZone(TimeZone.getDefault());
        return format.format(calendar.getTime());
    }

    public static String firstDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return format(calendar);
    }

    public static String lastDayOfMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return format(calendar);
    }

    /**
     * ISO day of week: 1 = Monday ... 7 = Sunday.
     */
    public static int isoDayOfWeek(String localDate) {
        Calendar calendar = parse(localDate);
        if (calendar == null) {
            return 1;
        }
        int javaDay = calendar.get(Calendar.DAY_OF_WEEK);
        if (javaDay == Calendar.SUNDAY) {
            return 7;
        }
        return javaDay - 1;
    }

    public static boolean isWeekday(String localDate) {
        int day = isoDayOfWeek(localDate);
        return day >= 1 && day <= 5;
    }

    public static int hourOfDay() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public static GreetingPeriod greetingPeriod() {
        int hour = hourOfDay();
        if (hour < 12) {
            return GreetingPeriod.MORNING;
        }
        if (hour < 18) {
            return GreetingPeriod.AFTERNOON;
        }
        return GreetingPeriod.EVENING;
    }

    public static long atTimeMillis(String localDate, int hour, int minute) {
        Calendar calendar = parse(localDate);
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static String formatTime(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    public static int[] parseTime(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) {
            return new int[]{9, 0};
        }
        String[] parts = hhmm.split(":");
        try {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (NumberFormatException exception) {
            return new int[]{9, 0};
        }
    }

    public static String formatClock(long epochMillis) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        format.setTimeZone(TimeZone.getDefault());
        return format.format(new Date(epochMillis));
    }

    public static String formatDateTime(long epochMillis) {
        SimpleDateFormat format = new SimpleDateFormat("d MMM HH:mm", new Locale("tr", "TR"));
        format.setTimeZone(TimeZone.getDefault());
        return format.format(new Date(epochMillis));
    }

    private static SimpleDateFormat usFormat() {
        SimpleDateFormat format = new SimpleDateFormat(PATTERN, Locale.US);
        format.setTimeZone(TimeZone.getDefault());
        return format;
    }
}
