package com.hsx.manyue.modules.video.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频搜索文档 - Elasticsearch 映射
 * 
 * 优化点：
 * 1. 使用 Elasticsearch Java API Client（co.elastic.clients）进行映射
 * 2. 移除 Spring Data Elasticsearch 注解依赖
 * 3. 添加 count、like、cover、tags 等字段，与数据库表结构保持一致
 * 4. 支持 function_score 评分所需的字段
 * 5. 使用 ik_max_word 分词器提升中文搜索效果（在索引创建时配置）
 */
@Data
public class VideoDocument {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("area")
    private String area;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("nick")
    private String nick;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 播放次数，用于 function_score 评分
    @JsonProperty("count")
    private Long count;
    
    // 点赞数，用于 function_score 评分（与 VideoEntity 字段名保持一致）
    @JsonProperty("like")
    private Long like;
    
    // 视频封面 URL
    @JsonProperty("cover")
    private String cover;
    
    // 视频标签，用于搜索匹配
    @JsonProperty("tags")
    private String tags;
    
    // 视频时长（秒）
    @JsonProperty("duration")
    private Float duration;
}
