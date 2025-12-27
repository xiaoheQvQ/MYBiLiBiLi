package com.hsx.manyue.ai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AIResponse {
    private String v; // 存储 AI 生成的文本内容
}