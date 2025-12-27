package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NewMessageDto {

    private String type;

    private VideoDTO data;
}
