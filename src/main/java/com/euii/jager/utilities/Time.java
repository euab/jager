package com.euii.jager.utilities;

import java.util.concurrent.TimeUnit;

public class Time {

    /**
     * Convert a millisecond duration to a human readable datetime, in Dd-Hh-Mm-Ss format.
     * https://stackoverflow.com/a/7663966
     *
     * @param millis A duration to convert to a human-readable string.
     * @return A string in the format Dd-Hh-Mm-Ss.
     */
    public static String makeHumanReadableTime(long millis) {

        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(days);
        sb.append(" Days ");
        sb.append(hours);
        sb.append(" Hours ");
        sb.append(minutes);
        sb.append(" Minutes ");
        sb.append(seconds);
        sb.append(" Seconds ");

        return sb.toString();
    }
}
