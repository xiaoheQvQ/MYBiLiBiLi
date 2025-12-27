package com.hsx.manyue.modules.video.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hsx.manyue.modules.video.model.dto.AddVideoSeriesDTO;
import com.hsx.manyue.modules.video.model.dto.SortVideoSeriesDTO;
import com.hsx.manyue.modules.video.model.dto.UpdateVideoSeriesDTO;
import com.hsx.manyue.modules.video.model.dto.VideoSeriesListVO;
import com.hsx.manyue.modules.video.model.entity.VideoEntity;
import com.hsx.manyue.modules.video.model.entity.VideoSeriesEntity;

public interface IvideoSeriesService extends IService<VideoSeriesEntity> {

    VideoSeriesListVO getVideoSeriesList(Long videoId);

    void addVideoSeries(AddVideoSeriesDTO dto);

    void updateVideoSeries(UpdateVideoSeriesDTO dto);

    void deleteVideoSeries(Long id, Long userId);

    void sortVideoSeries(SortVideoSeriesDTO dto);

}
