package com.hsx.manyue.ai.controller;
import com.hsx.manyue.ai.model.AIResponse;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 提示词模板（Prompt Template）功能演示控制器。
 * 此控制器展示了如何使用动态模板来构建提示，通过填充变量（如编程语言、功能描述）
 * 来生成结构化、特定角色的提示，从而引导 AI 模型产生更精确、更符合预期的输出。
 */
@RestController
@RequestMapping("/v7/ai")
public class PromptTemplateController {

    @Resource
    private OpenAiChatModel chatModel;

    /**
     * 使用简单的提示词模板生成智能代码。
     * @param message 功能描述
     * @param lang    编程语言
     * @return AI生成的代码流
     */
    @GetMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AIResponse> generateStream(@RequestParam(value = "message") String message,
                                           @RequestParam(value = "lang") String lang) {
        // 提示词模板
        String template = """
                你是一位资深 {lang} 开发工程师。请严格遵循以下要求编写代码：
                1. 功能描述：{description}
                2. 代码需包含详细注释
                3. 使用业界最佳实践
                """;

        PromptTemplate promptTemplate = new PromptTemplate(template);

        // 填充提示词占位符，转换为 Prompt 提示词对象
        Prompt prompt = promptTemplate.create(Map.of("description", message, "lang", lang));

        // 流式输出
        return chatModel.stream(prompt)
                .mapNotNull(chatResponse -> {
                    Generation generation = chatResponse.getResult();
                    String text = generation.getOutput().getText();
                    return AIResponse.builder().v(text).build();
                });
    }

    /**
     * 结合系统角色和用户角色的多轮提示词模板。
     * @param message 功能描述
     * @param lang    编程语言
     * @return AI生成的代码流
     */
    @GetMapping(value = "/generateStream3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AIResponse> generateStream3(@RequestParam(value = "message") String message,
                                            @RequestParam(value = "lang") String lang) {

        // 系统角色提示词模板
        String systemPrompt = """
                你是一位资深 {lang} 开发工程师, 已经从业数十年，经验非常丰富。
                """;
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        // 填充提示词占位符，并转换为 Message 对象
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("lang", lang));

        // 用户角色提示词模板
        String userPrompt = """
                请严格遵循以下要求编写代码：
                1. 功能描述：{description}
                2. 代码需包含详细注释
                3. 使用业界最佳实践
                """;
        PromptTemplate promptTemplate = new PromptTemplate(userPrompt);
        // 填充提示词占位符，并转换为 Message 对象
        Message userMessage = promptTemplate.createMessage(Map.of("description", message));


        // 组合多角色消息，构建提示词 Prompt
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        // 流式输出
        return chatModel.stream(prompt)
                .mapNotNull(chatResponse -> {
                    Generation generation = chatResponse.getResult();
                    String text = generation.getOutput().getText();
                    return AIResponse.builder().v(text).build();
                });
    }


}
