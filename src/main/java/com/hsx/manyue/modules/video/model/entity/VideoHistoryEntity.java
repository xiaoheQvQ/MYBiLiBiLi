package com.hsx.manyue.modules.video.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * 视频观看历史记录
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("t_video_history")
@Schema(name = "VideoHistoryEntity对象", description = "视频观看历史记录")
public class VideoHistoryEntity extends BaseEntity<VideoHistoryEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    private String ip;

    private String clientId;

    @Schema(description = "视频ID")
    private Long videoId;

    @Schema(description = "视频时间位置")
    private Double time;



    @Override
    public Serializable pkVal() {
        return null;
    }
}
