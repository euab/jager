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
