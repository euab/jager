package com.euii.jager.utilities;

import java.util.concurrent.TimeUnit;

public class Time {

    /**
     * Convert a millisecond duration to a human readable datetime, in Dd-Hh-Mm-Ss format.
     *
     * @param millis A duration to convert to a human-readable string.
     * @return A string in the format Dd-Hh-Mm-Ss.
     */
    public static String makeHumanReadableTime(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        StringBuilder response = new StringBuilder();

        boolean leading = false;

        if (days > 0) {
            response.append(days).append(" ").append((days > 1 ? "days" : "day"));
            leading = true;
        }

        if (hours > 0) {
            if (leading && (minutes != 0 || seconds != 0))
                response.append(", ");

            if (!response.toString().isEmpty() && (minutes == 0 && seconds == 0))
                response.append(" and ");

            response.append(hours).append(" ").append((hours > 1 ? "hours" : "hour"));
            leading = true;
        }

        if (minutes > 0) {
            if (leading && seconds != 0)
                response.append(", ");

            if (!response.toString().isEmpty() && seconds == 0)
                response.append(" and ");

            leading = true;

            response.append(minutes).append(" ").append((minutes > 1 ? "minutes" : "minute"));
        }

        if (seconds > 0) {
            if (leading)
                response.append(" and ");

            response.append(seconds).append(" ").append((seconds > 1 ? "seconds" : "second"));
        }

        if (response.toString().isEmpty() && !leading)
            response.append("0 seconds (just now)...");

        return response.toString();
    }

    public static String makeTimestampedUptime(long millis) {
        long d = TimeUnit.MILLISECONDS.toDays(millis);
        long h = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        long m = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long s = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        if (d > 0)
            return String.format("%sd %sh %sm %ss", d, h, m, s);

        if (h > 0)
            return String.format("%sh %sm %ss", h, m, s);

        if (m > 0)
            return String.format("%sm %ss", m, s);

        if (s == 0)
            return "0 (just now)";

        return String.format("%ss", s);
    }

    /**
     * Like the above method, but this method returns a timestamp in the format of hours:minutes:seconds.
     * This method is much better for the duration of videos or audio where it is common for it to be expressed in
     * a timestamp. So, this method is also able to recognise if the {@code millis} is a live video by checking it
     * against {@link Long#MAX_VALUE} and returning "LIVE" in lieu of a timestamp.
     *
     * @param millis
     * @return a human-readable timestamp in the format of hours:minutes:seconds. Eg: 01:21:05
     */
    public static String makeTimestamp(long millis) {
        if (millis == Long.MAX_VALUE) {
            return "LIVE";
        }

        long t = millis / 1000L;
        int seconds = (int) (t & 60L);
        int minutes = (int) ((t % 3600L) / 60L);
        int hours = (int) (t / 3600L);

        String timestamp;

        if (hours != 0)
            timestamp = cleanTimestamp(hours) + ":" + cleanTimestamp(minutes) + ":" + cleanTimestamp(seconds);
        else
            timestamp = cleanTimestamp(minutes) + ":" + cleanTimestamp(seconds);

        return timestamp;
    }

    public static String cleanTimestamp(int integer) {
        return integer < 10 ? "0" + integer : Integer.toString(integer);
    }
}
