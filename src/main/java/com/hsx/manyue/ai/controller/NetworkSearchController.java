package com.hsx.manyue.ai.controller;

import com.hsx.manyue.ai.advisor.NetworkSearchAdvisor;
import com.hsx.manyue.ai.model.SearchResult;
import com.hsx.manyue.ai.servcie.SearXNGService;
import com.hsx.manyue.ai.servcie.SearchResultContentFetcherService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 联网搜索控制器
 * 提供基于网络搜索的AI对话功能，通过整合搜索引擎结果增强AI回答的准确性和时效性
 *
 * @author hsx
 * @date 创建时间：2024年X月X日
 */
@RestController
@RequestMapping("/network")
public class NetworkSearchController {

    @Resource
    private SearXNGService searXNGService; // SearXNG搜索引擎服务

    @Resource
    private SearchResultContentFetcherService searchResultContentFetcherService; // 搜索结果内容提取服务

    @Resource
    private ChatClient chatClient; // Spring AI聊天客户端

    /**
     * 测试搜索引擎功能
     * 直接返回搜索引擎的原始搜索结果，用于调试和验证搜索功能
     *
     * @param message 搜索关键词或查询语句
     * @return 搜索引擎返回的原始结果列表
     */
    @GetMapping(value = "/test")
    public List<SearchResult> generateStream(@RequestParam(value = "message") String message) {
        // 调用 SearXNG 获取搜索结果
        List<SearchResult> searchResults = searXNGService.search(message);
        return searchResults;
    }

    /**
     * 流式对话接口
     * 结合网络搜索功能的AI对话，实时获取网络信息并生成回答
     * 使用SSE(Server-Sent Events)技术实现流式响应
     *
     * @param message 用户输入的消息或问题
     * @return 流式输出的HTML格式响应内容
     */
    @GetMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam(value = "message") String message) {
        // 创建并配置聊天请求，使用流式输出
        return chatClient.prompt()
                .user(message) // 设置用户输入的提示词
                .advisors(new NetworkSearchAdvisor(searXNGService, searchResultContentFetcherService)) // 添加联网搜索顾问
                .stream() // 启用流式处理
                .content(); // 获取内容流
    }
}