package com.hsx.manyue.ai.controller;

import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 文生视频
 * wanx2.1-i2v-turbo: 生成速度更快，耗时仅为plus模型的三分之一，性价比更高。计费价格为 0.24元/秒。
 * wanx2.1-i2v-plus: 生成细节更丰富，画面更具质感。计费价格为 0.70元/秒。
 **/
@RestController
@RequestMapping("/v12/ai")
@Slf4j
public class Text2VideoController {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    /**
     * 调用阿里百炼图生视频大模型
     * @param prompt
     * @return
     */
    @GetMapping("/text2video")
    public String text2video(@RequestParam(value = "prompt") String prompt) {
        // 设置视频处理参数，如指定输出视频的分辨率、视频时长等。
        Map<String, Object> parameters = new HashMap<>();
        // 是否开启 prompt 智能改写。开启后使用大模型对输入 prompt 进行智能改写。对于较短的 prompt 生成效果提升明显，但会增加耗时。
        parameters.put("prompt_extend", true);

        // 静态图片路径，将它转换为动态视频
        String imgUrl = "file:///" + "E:/xiaojiejie.png"; // Windows 系统
//        String imgUrl = "file://" + "/your/path/to/img.png"; // Linux/macOS 系统

        // 构建调用大模型所需参数
        VideoSynthesisParam param =
                VideoSynthesisParam.builder()
                        .apiKey(apiKey) // API Key
                        .model("wanx2.1-i2v-plus") // 模型名称
                        .prompt(prompt) // 提示词
                        .imgUrl(imgUrl) // 静态图片路径
                        .parameters(parameters) // 视频处理参数
                        .build();

        log.info("## 正在生成中, 请稍等...");

        // 调用 AI 大模型生成视频
        VideoSynthesis vs = new VideoSynthesis();
        VideoSynthesisResult result = null;
        try {
            result = vs.call(param);
        } catch (Exception e) {
            log.error("", e);
        }

        // 返参
        return JsonUtils.toJson(result);
    }

}