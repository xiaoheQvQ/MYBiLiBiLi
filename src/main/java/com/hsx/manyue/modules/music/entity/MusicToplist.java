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
 * 排行榜配置表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_music_toplist")
public class MusicToplist extends Model<MusicToplist> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 榜单名称
     */
    private String name;

    /**
     * 榜单描述
     */
    private String description;

    /**
     * 榜单图标
     */
    private String coverUrl;

    /**
     * 更新频率描述
     */
    private String updateFrequency;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
