package com.hsx.manyue.modules.admin;

import com.aliyuncs.exceptions.ClientException;
import com.hsx.manyue.common.dto.R;
import com.hsx.manyue.common.utils.VideoToWavUtil;
import com.hsx.manyue.modules.oss.service.IOssService;
import com.hsx.manyue.modules.oss.service.IVodService;
import com.hsx.manyue.modules.video.model.param.AdminVideoQueryParam;
import com.hsx.manyue.modules.video.service.IVideoService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import java.io.File;
import static com.hsx.manyue.common.dto.R.success;

/**
 * 视频表 前端控制器
 */
@RestController
@RequestMapping("/admin/video")
public class AdminVideoController {

    @Resource
    private IVideoService videoService;
    @Resource
    private  IVodService vodService;
    @Resource
    private  IOssService ossService;

    @GetMapping("/page")
    @Operation(summary = "分页搜索视频列表")
    public R pageVideo(AdminVideoQueryParam param) {
        return success(videoService.pageVideo(param));
    }

    @GetMapping("/Wav/{id}")
    @Operation(summary = "视频转字幕")
    public R WavSub(@PathVariable String id) throws ClientException {

           String videoPlayUrl = vodService.getVideoPlayUrl(id);
           try {
               // 2. 处理视频生成字幕
               String srtPath = VideoToWavUtil.videoUrlToVtt(videoPlayUrl);
               // 创建 File 对象
               File srtFile = new File(srtPath);

               // 调用 OSS 上传服务
               String ossUrl = ossService.uploadFile(srtFile);

               // 可选：上传完成后删除临时文件
               if (srtFile.exists()) {
                   srtFile.delete();
               }
               videoService.updateVideoSubtitle(id,ossUrl);


           } catch (Exception e) {
               throw new RuntimeException(e);

       }

        return R.success();
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除视频数据")
    public R deleteVideo(@PathVariable String id) {
        return success(videoService.deleteVideo(id));
    }

    @Operation(summary = "获取七日内视频数据,如果需要按天分组统计")
    @GetMapping("/selectUserVideoStatsGroupByDay")
    public R selectUserVideoStatsGroupByDay(String userId){
        Long id = Long.valueOf(userId);
        System.out.println("七日内："+videoService.selectUserVideoStatsGroupByDay(id));
        return success(videoService.selectUserVideoStatsGroupByDay(id));
    }

    @Operation(summary = "根据用户ID查询近7天的视频统计数据")
    @GetMapping("/getVideoStatsLast7Days")
    public R getVideoStatsLast7Days(String userId){
        Long id = Long.valueOf(userId);
        System.out.println("七日内："+videoService.getVideoStatsLast7Days(id));
        return success(videoService.getVideoStatsLast7Days(id));
    }
}
