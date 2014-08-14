package org.n3r.diamond.client;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BytesSize {
    public static Pattern bytesPattern =
            Pattern.compile("([\\d.]+)([GMK])B?", Pattern.CASE_INSENSITIVE);
    public static Map<String, Integer> powerMap =
            ImmutableMap.of("G", 3, "M", 2, "K", 1);


    public static long parseBytes(String str) {
        long returnValue = -1;
        Matcher matcher = bytesPattern.matcher(str);

        if (matcher.find()) {
            String number = matcher.group(1);
            int pow = powerMap.get(matcher.group(2).toUpperCase());
            BigDecimal bytes = new BigDecimal(number);
            bytes = bytes.multiply(BigDecimal.valueOf(1024).pow(pow));
            returnValue = bytes.longValue();
        }
        return returnValue;
    }
}
