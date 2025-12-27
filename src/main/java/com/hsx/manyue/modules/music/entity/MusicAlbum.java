package com.hsx.manyue.modules.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 专辑表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_music_album")
public class MusicAlbum extends BaseEntity<MusicAlbum> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 专辑标题
     */
    private String title;

    /**
     * 关联歌手ID
     */
    private Long artistId;

    /**
     * 歌手名(冗余)
     */
    private String artistName;

    /**
     * 专辑封面URL
     */
    private String coverUrl;

    /**
     * 专辑介绍
     */
    private String description;

    /**
     * 发行时间
     */
    private LocalDate publishTime;

    /**
     * 状态(0-下架, 1-上架)
     */
    private Boolean status;
}
