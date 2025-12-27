package com.hsx.manyue.modules.video.model.param;

import com.hsx.manyue.common.dto.PageParam;
import com.hsx.manyue.common.enums.VideoAreaEnum;
import lombok.Data;

@Data
public class VideoQueryParam extends PageParam {

    private VideoAreaEnum area;
    private String keyword;
    private String type = "video"; // 搜索类型：video-视频搜索，user-用户搜索
    private long seed;

}
