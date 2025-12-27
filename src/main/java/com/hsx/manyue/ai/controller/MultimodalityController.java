package com.hsx.manyue.ai.controller;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多模态能力（如图文识别）的控制器。
 * 本控制器用于处理结合了文本和图像的输入，调用具备视觉理解能力的 AI 模型(如 qwen-omni-turbo)，
 * 以实现对图片内容的提问和描述。
 */
@RestController
@RequestMapping("/v9/ai")
public class MultimodalityController {

    @Resource
    private OpenAiChatModel chatModel;

    /**
     * 接收文本和图片，进行流式对话
     * @param message 用户关于图片的提问
     * @return 返回一个包含 AI 模型对图片理解后生成的流式文本响应的 Flux<String>
     */
    @GetMapping(value = "/generateStream", produces = "text/html;charset=utf-8")
    public Flux<String> generateStream(@RequestParam(value = "message") String message) {
        // 1. 创建媒体资源
        Media image = new Media(
                MimeTypeUtils.IMAGE_PNG,
                new ClassPathResource("/images/multimodal-test.png")
        );

        // 2. 附加选项（可选），如温度值等等
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("temperature", 0.7);

        // 3. 构建多模态消息
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(image)
                .metadata(metadata)
                .build();

        // 4. 构建提示词
        Prompt prompt = new Prompt(List.of(userMessage));
        // 5. 流式调用
        return chatModel.stream(prompt)
                .mapNotNull(chatResponse -> {
                    Generation generation = chatResponse.getResult();
                    return generation.getOutput().getText();
                });
    }

}
