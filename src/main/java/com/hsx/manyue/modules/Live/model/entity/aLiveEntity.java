package com.hsx.manyue.modules.Live.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hsx.manyue.common.model.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_live_stream")
public class aLiveEntity  extends BaseEntity<aLiveEntity> {

    private static final long serialVersionUID = 1L;


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
    private int isLiving;


    @Schema(description = "历史记录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiredTime;




    @Override
    public Serializable pkVal() {
        return null;
    }
}
