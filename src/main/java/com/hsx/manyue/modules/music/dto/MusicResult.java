package com.hsx.manyue.modules.music.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TuneHub 兼容响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicResult {
    private Integer code;
    private String message;
    private Object data;
    private String timestamp;

    public static MusicResult success(Object data) {
        return new MusicResult(200, "success", data, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    public static MusicResult error(String message) {
        return new MusicResult(500, message, null, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
