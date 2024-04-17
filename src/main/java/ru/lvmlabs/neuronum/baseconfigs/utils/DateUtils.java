package ru.lvmlabs.neuronum.baseconfigs.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public final class DateUtils {
    public static String toString(Date date, String pattern) {
        if (date == null) return toString(new Date(), pattern);

        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date fromString(String dateTemplate, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(dateTemplate);
        } catch (ParseException e) {
            log.error("Can't parse the date: {} with pattern: {}", dateTemplate, pattern);
            e.printStackTrace();
        }

        return new Date();
    }
}
