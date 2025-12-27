package com.hsx.manyue.ai.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 检索增强生成（RAG - Retrieval-Augmented Generation）控制器。
 * 该控制器通过结合外部知识库（向量数据库）来增强大语言模型的能力。
 * 当用户提问时，它首先从向量库中检索相关文档，然后将这些文档作为上下文信息
 * 一同提供给 AI，从而生成基于特定知识的、更准确的回答。
 **/
@RestController
@RequestMapping("/rag")
public class RAGController {

    @Resource
    private VectorStore vectorStore;
    @Resource
    private ChatClient chatClient;

    /**
     * 流式对话
     * @param message 用户的问题
     * @return 增强后的AI回答流
     */
    @GetMapping(value = "/generateStream", produces = "text/html;charset=utf-8")
    public Flux<String> generateStream(@RequestParam(value = "message") String message) {

        // 流式输出
        return chatClient.prompt()
                .system("请你扮演一名企业客服。从企业内部知识库中查阅相关资料，并回答用户，若内部资料没有相关内容，则回答 “未找到相关资料”")
                .user(message) // 提示词
                .advisors(new QuestionAnswerAdvisor(vectorStore)) // 检索向量库，组合增强提示词，调用 AI 大模型
                .stream()
                .content();

    }

}
