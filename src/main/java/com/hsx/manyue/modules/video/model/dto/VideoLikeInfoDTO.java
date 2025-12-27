package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;

/**
 * 是否喜欢、收藏视频
 */
@Data

public class VideoLikeInfoDTO {
    private Boolean isLike;
    private Boolean isCollect;
}
