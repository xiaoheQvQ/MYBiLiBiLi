package com.hsx.manyue.ai.servcie;

import com.hsx.manyue.ai.model.SearchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 页面内容提取
 **/
public interface SearchResultContentFetcherService {


    /**
     * 并发批量获取搜索结果页面的内容
     *
     * @param searchResults
     * @param timeout
     * @param unit
     * @return
     */
    CompletableFuture<List<SearchResult>> batchFetch(List<SearchResult> searchResults,
                                                     long timeout,
                                                     TimeUnit unit);
}