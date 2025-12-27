package com.hsx.manyue.modules.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 歌手表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_music_artist")
public class MusicArtist extends BaseEntity<MusicArtist> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 歌手名称
     */
    private String name;

    /**
     * 歌手别名/英文名
     */
    private String alias;

    /**
     * 歌手头像URL
     */
    private String avatar;

    /**
     * 歌手简介
     */
    private String description;

    /**
     * 地区(1-华语,2-欧美,3-日韩,0-其他)
     */
    private Integer area;

    /**
     * 单曲数量(冗余字段)
     */
    private Integer songCount;

    /**
     * 专辑数量(冗余字段)
     */
    private Integer albumCount;
}
