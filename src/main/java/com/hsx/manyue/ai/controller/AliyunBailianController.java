package com.hsx.manyue.ai.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 对话控制器，用于处理标准、有状态的聊天会话。
 * 它通过内存中的 ConcurrentHashMap 来管理每个用户的聊天记录，
 * 实现了基于上下文的连续对话功能。
 */
@RestController
@RequestMapping("/v6/ai")
public class AliyunBailianController {

    @Resource
    private OpenAiChatModel chatModel;

    private Map<String, List<Message>> chatMemoryStore = new ConcurrentHashMap<>();

    /**
     * 普通对话，支持上下文记忆。
     * @param message 用户发送的消息
     * @param chatId  用于区分不同对话会话的唯一标识
     * @return AI模型的回复内容
     */
    @GetMapping("/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "你是谁？") String message,
                           @RequestParam(value = "chatId") String chatId) {
        // 根据 chatId 获取对话记录
        List<Message> messages = chatMemoryStore.get(chatId);
        // 若不存在，则初始化一份
        if (CollectionUtils.isEmpty(messages)) {
            messages = new ArrayList<>();
            chatMemoryStore.put(chatId, messages);
        }

        // 添加 “用户角色消息” 到聊天记录中
        messages.add(new UserMessage(message));

        // 构建提示词
        Prompt prompt = new Prompt(messages);
        // 一次性返回结果
        String responseText = chatModel.call(prompt).getResult().getOutput().getText();

        // 添加 “助手角色消息” 到聊天记录中
        messages.add(new AssistantMessage(responseText));

        return responseText;
    }


}
