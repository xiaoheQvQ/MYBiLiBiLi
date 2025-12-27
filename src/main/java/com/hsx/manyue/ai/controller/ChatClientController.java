package com.hsx.manyue.ai.controller;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 基于 Spring AI ChatClient 的流式对话控制器。
 * ChatClient 提供了更高级、更流畅的 API，简化了与 AI 模型的交互，
 * 并内置了对聊天记忆（ChatMemory）的支持，适用于构建流式响应的聊天应用。
 */
@RestController
@RequestMapping("/v2/ai")
public class ChatClientController {

    /**
     * @param ChatClient
     * 可适配不同 ChatModel 实现
     * 输出/输出 标准化的 Prompt 和 ChatResponse
     * 不支持细粒度模型参数（如 temperature）
     * @return
     */
    @Resource
    private ChatClient chatClient;


    /**
     * 流式对话
     * @param message 用户发送的消息
     * @param chatId  用于区分不同对话会话的唯一标识
     * @return 返回一个包含 AI 模型流式响应的 Flux<String>
     */
    @GetMapping(value = "/generateStream", produces = "text/html;charset=utf-8")
    public Flux<String> generateStream(@RequestParam(value = "message", defaultValue = "你是谁？") String message,
                                       @RequestParam(value = "chatId") String chatId
    ) {

        // 流式输出
        return chatClient.prompt()
//                .system("请你扮演一名小何 Java 项目实战专栏的客服人员")
                .user(message) // 提示词
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();

    }

}
