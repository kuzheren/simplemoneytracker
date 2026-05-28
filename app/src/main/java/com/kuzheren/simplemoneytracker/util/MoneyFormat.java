package com.kuzheren.simplemoneytracker.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormat {

    private static final DecimalFormat INT_FORMAT;
    private static final DecimalFormat DEC_FORMAT;

    static {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        sym.setGroupingSeparator(' ');
        sym.setDecimalSeparator(',');
        INT_FORMAT = new DecimalFormat("#,##0", sym);
        DEC_FORMAT = new DecimalFormat("#,##0.00", sym);
    }

    public static String format(double v) {
        if (Math.abs(v - Math.rint(v)) < 0.005) {
            return INT_FORMAT.format(v) + " ₽";
        }
        return DEC_FORMAT.format(v) + " ₽";
    }

    private MoneyFormat() {}
}
