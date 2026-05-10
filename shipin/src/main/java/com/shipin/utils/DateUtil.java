package com.shipin.utils;


import com.shipin.entity.enums.DateTimePatternEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtil {

    private static final Object lockObj = new Object();
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
        if (tl == null) {
            synchronized (lockObj) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    tl = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }

        return tl.get();
    }

    public static String format(Date date, String pattern) {
        return getSdf(pattern).format(date);
    }

    public static Date parse(String dateStr, String pattern) {
        try {
            return getSdf(pattern).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }
    public static Date formatDateToDay(Date date) {
        if (date == null) {
            return null;
        }
        String dateStr = getSdf(DateTimePatternEnum.YYYY_MM_DD.getPattern()).format(date);
        return parse(dateStr, DateTimePatternEnum.YYYY_MM_DD.getPattern());
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if(date1==null||date2==null){
            return false;
        }
        return formatDateToDay(date1).equals(formatDateToDay(date2));
    }
}
