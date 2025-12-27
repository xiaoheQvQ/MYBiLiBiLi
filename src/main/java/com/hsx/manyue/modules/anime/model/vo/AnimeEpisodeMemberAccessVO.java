package com.hsx.manyue.modules.anime.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnimeEpisodeMemberAccessVO {
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 分集ID
     */
    private Long episodeId;
    
    /**
     * 最低会员等级要求(1-普通,2-高级,3-尊享)
     */
    private Integer minMemberLevel;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}