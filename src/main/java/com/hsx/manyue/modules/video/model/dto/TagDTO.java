package com.hsx.manyue.modules.video.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema( name="标签")
@Data
public class TagDTO {

    @Schema(description = "id")
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
}
