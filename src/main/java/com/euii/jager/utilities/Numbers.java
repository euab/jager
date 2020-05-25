package com.euii.jager.utilities;

import java.text.DecimalFormat;

public class Numbers {

    // Just a bunch of utility methods to help with processing numbers.
    // These are just extensions of java.lang methods which exist to make my life easier.

    private static final DecimalFormat GLOBAL_DECIMAL_FORMAT = new DecimalFormat("#,###,##");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0");

    public static int parseInt(String string) {
        return parseInt(string, 0);
    }

    public static int parseInt(String string, int def) {
        return parseInt(string, def,  10);
    }

    public static int parseInt(String string, int def, int radix) {
        try {
            return Integer.parseInt(string, radix);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static String formatDecimals(float n) {
        return GLOBAL_DECIMAL_FORMAT.format(n);
    }

    public static String formatDecimals(double n) {
        return GLOBAL_DECIMAL_FORMAT.format(n);
    }

    public static String formatValue(int n) {
        return NUMBER_FORMAT.format(n);
    }
}
