package com.hsx.manyue.modules.video.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_video_series")
public class VideoSeriesEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 系列ID(同一系列的分P视频共享此ID)
     */
    private Long seriesId;

    /**
     * 视频ID(关联t_video.id)
     */
    private Long videoId;

    /**
     * 分P标题
     */
    private String title;

    /**
     * 分P描述
     */
    private String description;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删除，1-已删除)
     */
    private Integer isDelete;
}