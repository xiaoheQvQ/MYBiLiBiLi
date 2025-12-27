package com.hsx.manyue.modules.chat.model.dto;

import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NewChatMessageDto {

    private String type;

    private ChatMessage data;

}

