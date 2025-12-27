package com.hsx.manyue.modules.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 排行榜歌曲关联表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_music_toplist_relation")
public class MusicToplistRelation extends Model<MusicToplistRelation> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 榜单ID
     */
    private Long toplistId;

    /**
     * 歌曲ID
     */
    private Long songId;

    /**
     * 排名(1, 2, 3...)
     */
    private Integer rankNum;

    /**
     * 热度分数/播放量快照
     */
    private Integer score;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
