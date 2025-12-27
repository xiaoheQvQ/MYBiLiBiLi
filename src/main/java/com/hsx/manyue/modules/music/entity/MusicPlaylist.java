package com.hsx.manyue.modules.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 歌单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_music_playlist")
public class MusicPlaylist extends BaseEntity<MusicPlaylist> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建者ID
     */
    private Long userId;

    /**
     * 歌单标题
     */
    private String title;

    /**
     * 歌单封面
     */
    private String coverUrl;

    /**
     * 歌单描述
     */
    private String description;

    /**
     * 播放量
     */
    private Integer playCount;

    /**
     * 是否公开(1-公开, 0-私密)
     */
    private Boolean isPublic;
}
