package com.hyapp.achat.viewmodel.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String millis2DayTime(long millis) {
        return new SimpleDateFormat("h:mm", Locale.getDefault()).format(new Date(millis));
    }

    public static String timeAgoShort(long duration) {
        long[] times = new long[]{
                TimeUnit.DAYS.toMillis(365)
                , TimeUnit.DAYS.toMillis(30)
                , TimeUnit.DAYS.toMillis(7)
                , TimeUnit.DAYS.toMillis(1)
                , TimeUnit.HOURS.toMillis(1)
                , TimeUnit.MINUTES.toMillis(1)
                , TimeUnit.SECONDS.toMillis(1)
        };
        String[] timesString = new String[]{"y", "mo", "w", "d", "h", "m", "s"};

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times.length; i++) {
            long current = times[i];
            long temp = duration / current;
            if (temp > 0) {
                builder.append(temp).append(timesString[i]);
                break;
            }
        }
        return builder.toString();
    }

    public static long durationToMillis(String duration) {
        String[] parts = duration.split(":");
        long result = 0;
        for (int i = parts.length - 1, j = 1000; i >= 0; i--, j *= 60) {
            try {
                result += Integer.parseInt(parts[i]) * j;
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public static String millisToDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        if (hours == 0) {
            return String.format(Locale.US, "%02d:%02d"
                    , TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))
                    , TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }
        return String.format(Locale.US, "%02d:%02d:%02d"
                , hours
                , TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))
                , TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

}
