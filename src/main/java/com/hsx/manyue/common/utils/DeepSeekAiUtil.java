package com.hsx.manyue.common.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DeepSeekAiUtil {
    private static final String API_KEY = "sk-c0d80b17730d4928a0234bd921363612";
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    // 使用ConcurrentHashMap存储每个用户的对话历史
    private static final Map<Long, List<JSONObject>> userChatHistories = new ConcurrentHashMap<>();
    // 每个用户的历史记录最大条数，防止token过长
    private static final int MAX_HISTORY_SIZE = 10;

    /**
     * 为用户创建新的对话会话
     * @param userId 用户ID
     */
    public void createNewChatSession(Long userId) {
        userChatHistories.put(userId, new ArrayList<>());
    }

    /**
     * 清除用户的对话历史
     * @param userId 用户ID
     */
    public void clearChatHistory(Long userId) {
        userChatHistories.remove(userId);
    }

    /**
     * 使用对话历史进行AI对话
     * @param userId 用户ID
     * @param prompt 用户输入的问题
     * @return AI回复内容
     */
    public String doChat(Long userId, String prompt) {
        // 获取用户的对话历史，如果不存在则创建
        List<JSONObject> chatHistory = userChatHistories.computeIfAbsent(userId, k -> new ArrayList<>());

        // 创建用户消息
        JSONObject userMessage = JSONUtil.createObj()
                .set("role", "user")
                .set("content", prompt);

        // 将用户消息添加到历史记录
        chatHistory.add(userMessage);

        // 如果历史记录超过最大长度，则移除最早的消息
        if (chatHistory.size() > MAX_HISTORY_SIZE) {
            chatHistory.remove(0);
        }

        try {
            // 创建完整的消息历史数组
            JSONArray messagesArray = JSONUtil.createArray();
            for (JSONObject message : chatHistory) {
                messagesArray.add(message);
            }

            // 构建请求体
            JSONObject requestBody = JSONUtil.createObj()
                    .set("model", "deepseek-chat")
                    .set("messages", messagesArray)
                    .set("frequency_penalty", 0)
                    .set("presence_penalty", 0)
                    .set("stop", null)
                    .set("stream", false)
                    .set("stream_options", null)
                    .set("temperature", 1)
                    .set("top_p", 1)
                    .set("tools", null)
                    .set("tool_choice", "none")
                    .set("logprobs", false)
                    .set("top_logprobs", null);

            // 记录请求信息便于调试
            log.debug("API请求URL: {}", API_URL);
            log.debug("API请求体: {}", requestBody.toString());

            // 发送请求
            HttpResponse httpResponse = HttpRequest.post(API_URL)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .body(requestBody.toString())
                    .execute();

            String result = httpResponse.body();
            int status = httpResponse.getStatus();

            // 记录响应状态和内容
            log.debug("API响应状态: {}", status);
            log.debug("API响应内容: {}", result);

            // 如果状态不是200，打印详细信息
            if (status != HttpStatus.HTTP_OK) {
                log.error("深度AI调用失败，状态码: {}, 响应内容: {}", status, result);
                return "AI服务暂时不可用，请稍后再试。";
            }

            // 解析响应
            JSONObject response = JSONUtil.parseObj(result);
            if (response.containsKey("error")) {
                String errorMessage = response.getByPath("error.message", String.class);
                log.error("深度AI调用失败，错误信息: {}", errorMessage);
                return "AI服务返回错误: " + errorMessage;
            }

            // 获取AI回复内容
            String aiContent = response.getByPath("choices[0].message.content", String.class);

            // 将AI回复添加到历史记录
            JSONObject assistantMessage = JSONUtil.createObj()
                    .set("role", "assistant")
                    .set("content", aiContent);
            chatHistory.add(assistantMessage);

            // 如果历史记录超过最大长度，则移除最早的消息
            if (chatHistory.size() > MAX_HISTORY_SIZE) {
                chatHistory.remove(0);
            }

            return aiContent;
        } catch (Exception e) {
            log.error("深度AI调用异常", e);
            return "AI服务请求异常: " + e.getMessage();
        }
    }

    /**
     * 向已有对话中追加系统消息（用于设置上下文或规则）
     * @param userId 用户ID
     * @param systemMessage 系统消息内容
     */
    public void addSystemMessage(Long userId, String systemMessage) {
        List<JSONObject> chatHistory = userChatHistories.computeIfAbsent(userId, k -> new ArrayList<>());
        JSONObject sysMessage = JSONUtil.createObj()
                .set("role", "system")
                .set("content", systemMessage);

        // 系统消息通常放在对话的开始
        chatHistory.add(0, sysMessage);

        // 控制历史长度
        if (chatHistory.size() > MAX_HISTORY_SIZE) {
            // 移除最早的用户或AI消息，但保留系统消息
            for (int i = 1; i < chatHistory.size(); i++) {
                if (!"system".equals(chatHistory.get(i).getStr("role"))) {
                    chatHistory.remove(i);
                    break;
                }
            }
        }
    }

    // 保留兼容旧接口
    public String doChat(String prompt) {
        // 为临时用户创建一个独立会话
        Long tempUserId = System.currentTimeMillis();
        String result = doChat(tempUserId, prompt);
        // 清除临时会话
        clearChatHistory(tempUserId);
        return result;
    }
}
