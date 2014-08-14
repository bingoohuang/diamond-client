package org.n3r.diamond.client;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Duration {
    public static Pattern durationPattern = Pattern
            .compile("((\\d+)[dD])?\\s*((\\d+)[hH])?\\s*((\\d+)[mM])?\\s*((\\d+)[Ss])?");

    public static long getDuration(String duration, TimeUnit timeUnit) {
        Matcher m = durationPattern.matcher(duration);
        m.find();

        long miliSeconds = 0;

        TimeUnit millisecondsTimeUnit = TimeUnit.MILLISECONDS;
        if (StringUtils.isNotEmpty(m.group(2))) {
            int days = Integer.parseInt(m.group(2));
            miliSeconds += millisecondsTimeUnit.convert(days, TimeUnit.DAYS);
        }

        if (StringUtils.isNotEmpty(m.group(4))) {
            int hours = Integer.parseInt(m.group(4));
            miliSeconds += millisecondsTimeUnit.convert(hours, TimeUnit.HOURS);
        }

        if (StringUtils.isNotEmpty(m.group(6))) {
            int minutes = Integer.parseInt(m.group(6));
            miliSeconds += millisecondsTimeUnit.convert(minutes, TimeUnit.MINUTES);
        }

        if (StringUtils.isNotEmpty(m.group(8))) {
            int seconds = Integer.parseInt(m.group(8));
            miliSeconds += millisecondsTimeUnit.convert(seconds, TimeUnit.SECONDS);
        }

        return timeUnit.convert(miliSeconds, millisecondsTimeUnit);
    }
}
