package com.hsx.manyue.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeConvertUtil {

    public static String formatTimeDifference(long timeDifference) {
        long SECOND = 1000; // 1秒 = 1000毫秒
        long MINUTE = 60 * SECOND; // 1分钟 = 60秒
        long HOUR = 60 * MINUTE; // 1小时 = 60分钟
        long DAY = 24 * HOUR; // 1天 = 24小时
        long WEEK = 7 * DAY; // 1周 = 7天

        if (timeDifference < MINUTE) {
            // 不足1分钟
            return "刚刚";
        } else if (timeDifference < HOUR) {
            // 不足1小时
            long minutes = timeDifference / MINUTE;
            return minutes + "分钟前";
        } else if (timeDifference < DAY) {
            // 不足1天
            long hours = timeDifference / HOUR;
            return hours + "小时前";
        } else if (timeDifference < WEEK) {
            // 不足1周
            long days = timeDifference / DAY;
            return days + "天前";
        } else {
            // 大于等于1周，直接显示日期
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateFormat.format(new Date());
        }
    }
}