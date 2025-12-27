package com.hsx.manyue.modules.anime.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("anime_episode_member_access")
public class AnimeEpisodeMemberAccessEntity extends BaseEntity<AnimeEpisodeMemberAccessEntity> {
    /**
     * 分集ID
     */
    private Long episodeId;
    
    /**
     * 最低会员等级要求(1-普通,2-高级,3-尊享)
     */
    private Integer minMemberLevel;
}