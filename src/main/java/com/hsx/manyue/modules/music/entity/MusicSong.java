package com.hsx.manyue.modules.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hsx.manyue.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 歌曲主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_music_song")
public class MusicSong extends BaseEntity<MusicSong> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 歌曲标题
     */
    private String title;

    /**
     * 歌手ID
     */
    private Long artistId;

    /**
     * 歌手名(冗余)
     */
    private String artistName;

    /**
     * 专辑ID
     */
    private Long albumId;

    /**
     * 专辑名(冗余)
     */
    private String albumName;

    /**
     * 音频文件URL(本地OSS或外部链接)
     */
    private String fileUrl;

    /**
     * 封面图(通常取自专辑，也可单独覆盖)
     */
    private String coverUrl;

    /**
     * 歌词文件(.lrc)URL
     */
    private String lyricUrl;

    /**
     * 歌词文本(若没有lrc文件则存文本)
     */
    private String lyricText;

    /**
     * 时长(秒)
     */
    private Integer duration;

    /**
     * 最高音质标识(128k/320k/flac)
     */
    private String quality;

    /**
     * 是否仅会员(1-是, 0-否)
     */
    private Boolean isVipOnly;

    /**
     * 状态(0-审核中, 1-已发布, 2-下架)
     */
    private Integer status;

    /**
     * 来源标识(local/netease/import)
     */
    private String sourceType;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 播放次数
     */
    private Long playCount;
}
