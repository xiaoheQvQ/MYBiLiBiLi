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
 * 歌单歌曲关联表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_music_playlist_relation")
public class MusicPlaylistRelation extends Model<MusicPlaylistRelation> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 歌单ID
     */
    private Long playlistId;

    /**
     * 歌曲ID
     */
    private Long songId;

    /**
     * 排序(越小越前)
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
