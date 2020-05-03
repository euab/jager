package com.euii.jager.utilities;

public class Numbers {

    // Just a bunch of utility methods to help with processing numbers.
    // These are just extensions of java.lang methods which exist to make my life easier.

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
}
