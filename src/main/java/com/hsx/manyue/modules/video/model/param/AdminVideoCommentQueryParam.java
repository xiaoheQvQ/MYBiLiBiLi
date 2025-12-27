package com.hsx.manyue.modules.video.model.param;

import com.hsx.manyue.common.dto.PageParam;
import lombok.Data;

@Data
public class AdminVideoCommentQueryParam extends PageParam {
    private String userName;

    private String content;
}
