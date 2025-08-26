package com.feeltens.git.util;

import java.time.OffsetDateTime;
import java.util.Date;

public class DateTimeXUtil {

    /**
     * ISO 8601 格式字符串转换为 Date
     *
     * @param isoString ISO 8601 格式字符串
     * @return date
     */
    public static Date isoString2Date(String isoString) {
        // 1. 解析为带时区的 OffsetDateTime
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(isoString);

        // 2. 转换为系统默认时区的瞬时时间
        return Date.from(offsetDateTime.toInstant());
    }

}