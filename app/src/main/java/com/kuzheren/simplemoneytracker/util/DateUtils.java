package com.kuzheren.simplemoneytracker.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat YEAR_MONTH = new SimpleDateFormat("yyyy-MM", Locale.US);
    private static final SimpleDateFormat MONTH_LABEL = new SimpleDateFormat("LLLL yyyy", new Locale("ru"));
    private static final SimpleDateFormat HEADER = new SimpleDateFormat("d MMMM", new Locale("ru"));
    private static final SimpleDateFormat HUMAN = new SimpleDateFormat("d MMMM yyyy", new Locale("ru"));

    public static String todayIso() {
        return ISO.format(new Date());
    }

    public static String toIso(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month, day);
        return ISO.format(c.getTime());
    }

    public static String currentYearMonth() {
        return YEAR_MONTH.format(new Date());
    }

    public static String yearMonthOf(String iso) {
        if (iso == null || iso.length() < 7) return "";
        return iso.substring(0, 7);
    }

    public static String formatMonthLabel() {
        return formatMonthLabel(currentYearMonth());
    }

    public static String formatMonthLabel(String yearMonth) {
        Calendar c = yearMonthToCalendar(yearMonth);
        String s = MONTH_LABEL.format(c.getTime());
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String shiftYearMonth(String yearMonth, int months) {
        Calendar c = yearMonthToCalendar(yearMonth);
        c.add(Calendar.MONTH, months);
        return YEAR_MONTH.format(c.getTime());
    }

    private static Calendar yearMonthToCalendar(String yearMonth) {
        Calendar c = Calendar.getInstance();
        try {
            int year = Integer.parseInt(yearMonth.substring(0, 4));
            int month = Integer.parseInt(yearMonth.substring(5, 7)) - 1;
            c.clear();
            c.set(year, month, 1);
        } catch (Exception ignored) {
        }
        return c;
    }

    public static String formatHeader(String iso) {
        Date d = parse(iso);
        if (d == null) return iso;
        Calendar today = Calendar.getInstance();
        Calendar that = Calendar.getInstance();
        that.setTime(d);
        if (sameDay(today, that)) return "Сегодня";
        today.add(Calendar.DAY_OF_YEAR, -1);
        if (sameDay(today, that)) return "Вчера";
        return HEADER.format(d);
    }

    public static String formatHuman(String iso) {
        Date d = parse(iso);
        return d == null ? iso : HUMAN.format(d);
    }

    public static Calendar parseToCalendar(String iso) {
        Date d = parse(iso);
        Calendar c = Calendar.getInstance();
        if (d != null) c.setTime(d);
        return c;
    }

    private static Date parse(String iso) {
        if (iso == null) return null;
        try {
            return ISO.parse(iso);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private DateUtils() {}
}
