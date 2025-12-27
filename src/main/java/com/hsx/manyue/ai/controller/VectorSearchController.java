package com.hsx.manyue.ai.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 向量数据库检索控制器。
 * 该控制器提供了一个直接访问和查询向量数据库的接口。
 * 它允许用户通过关键词进行相似性搜索，以验证向量化数据的存储和检索效果，
 * 是调试 RAG 流程中知识库检索环节的重要工具。
 **/
@RestController
@RequestMapping("/vector")
public class VectorSearchController {

    @Resource
    private VectorStore vectorStore;

    /**
     * 检索向量数据库
     * @param key 查询的关键词
     * @return 返回与关键词最相似的文档列表
     */
    @GetMapping(value = "/search")
    public List<Document> search(@RequestParam(value = "key") String key) {
        // 检索与查询相似的文档
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query(key) // 查询的关键词
                .topK(2) // 查询相似度最高的 2 条文档
                .build());

        return results;
    }

}
