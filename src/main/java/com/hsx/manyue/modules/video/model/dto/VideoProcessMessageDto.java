package com.hsx.manyue.modules.video.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class VideoProcessMessageDto implements Serializable {
    private Long videoId;
    private byte[] videoBytes;
    private String subtitle;

}