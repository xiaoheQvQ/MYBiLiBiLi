package com.hsx.manyue.ai.servcie;


import com.hsx.manyue.ai.model.SearchResult;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface SearXNGService {

    /**
     * 调用 SearXNG Api, 获取搜索结果
     * @param query 搜索关键词
     * @return
     */
    List<SearchResult> search(String query);


}