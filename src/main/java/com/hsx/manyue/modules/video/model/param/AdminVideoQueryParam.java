package com.hsx.manyue.modules.video.model.param;

import com.hsx.manyue.common.dto.PageParam;
import lombok.Data;

@Data
public class AdminVideoQueryParam extends PageParam {

    private String userName;

    private String keyword;
}
