package com.hsx.manyue.modules.Live.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;


import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Schema(name = "直播间信息")
public class aLiveDTO {


    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 推流码
     */
    private String streamKey;

    /**
     * 推流地址
     */
    private String pushUrl;

    /**
     * 直播状态
     */
    private boolean isLiving;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")

    private LocalDateTime createTime;


    @Schema(name = "历史记录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;



    private String userNickname;

    private String userAvatar;

    private String title;

    private String coverUrl;

    private Integer viewerCount;

}
