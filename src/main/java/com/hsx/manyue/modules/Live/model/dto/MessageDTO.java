package com.hsx.manyue.modules.Live.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@Accessors(chain = true)
public class MessageDTO {

    /**
     * 开播者ID
     */
    private Long publishId;

    /**
     * 开播者昵称
     * */
    private String UpName;

    /**
     * 粉丝ID
     */
    private Long userId;


}
