package com.hsx.manyue.modules.video.search.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsx.manyue.modules.video.model.dto.VideoDTO;
import com.hsx.manyue.modules.video.model.param.VideoQueryParam;
import com.hsx.manyue.modules.video.search.VideoSearchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import java.io.StringReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
/**
 * Elasticsearch增强搜索策略
 * 优化点：
 * 1. function_score自定义评分（结合播放量、点赞数、时间衰减）
 * 2. 高亮显示搜索关键词
 * 3. 分词搜索（ik_max_word分词器）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchEnhancedSearchStrategy implements VideoSearchStrategy {

    private final ElasticsearchClient esClient;
    
    private static final String VIDEO_INDEX = "video_index";

    @Override
    public boolean supports(String type) {
        return "es".equals(type) || "elasticsearch".equals(type);
    }

    @Override
    public IPage<VideoDTO> search(VideoQueryParam param) {
        try {
            String keyword = param.getKeyword();
            
            // 1. 构建基础查询（使用multi_match多字段匹配 + ik分词）
            Query baseQuery = buildBaseQuery(keyword);
            
            // 2. 构建function_score查询（自定义评分）
            Query functionScoreQuery = buildFunctionScoreQuery(baseQuery);
            
            // 3. 构建高亮配置
            Highlight highlight = buildHighlight();
            
            // 4. 执行搜索
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(VIDEO_INDEX)
                    .query(functionScoreQuery)
                    .highlight(highlight)
                    .from((int) ((param.getCurrent() - 1) * param.getSize()))
                    .size((int) param.getSize())
                    .sort(sort -> sort
                            .score(score -> score.order(SortOrder.Desc))
                    )
            );
            
            SearchResponse<VideoDTO> response = esClient.search(searchRequest, VideoDTO.class);
            
            // 5. 解析结果
            return parseSearchResponse(response, param);
            
        } catch (Exception e) {
            log.error("Elasticsearch搜索失败", e);
            // 降级：返回空结果
            return new Page<>(param.getCurrent(), param.getSize());
        }
    }

    /**
     * 构建基础查询（multi_match + ik分词）
     */
    private Query buildBaseQuery(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Query.of(q -> q.matchAll(ma -> ma));
        }
        
        // 使用multi_match在多个字段中搜索，并使用ik_max_word分词器
        return Query.of(q -> q.multiMatch(m -> m
                .query(keyword)
                .fields("title^3", "description^2", "tags^1.5") // 标题权重最高
                .type(TextQueryType.BestFields) // 最佳字段匹配
                .tieBreaker(0.3) // 其他字段的分数系数
        ));
    }

    /**
     * 构建function_score查询（自定义评分）
     * 综合考虑：
     * 1. 文本相关性（基础分）
     * 2. 播放量加权
     * 3. 点赞数加权
     * 4. 时间衰减（越新的视频分数越高）
     */
    private Query buildFunctionScoreQuery(Query baseQuery) {
        // 将原生的 functions 数组部分写成 JSON 字符串
        String functionsJson = "[" +
                "  { \"field_value_factor\": { \"field\": \"count\", \"factor\": 0.0001, \"modifier\": \"log1p\", \"missing\": 0.0 }, \"weight\": 1.0 }," +
                "  { \"field_value_factor\": { \"field\": \"like\", \"factor\": 0.001, \"modifier\": \"log1p\", \"missing\": 0.0 }, \"weight\": 2.0 }," +
                "  { \"exp\": { \"createTime\": { \"origin\": \"now\", \"scale\": \"30d\", \"offset\": \"7d\", \"decay\": 0.5 } }, \"weight\": 3.0 }" +
                "]";

        return Query.of(q -> q.functionScore(fs -> fs
                .query(baseQuery)
                // 使用 withJson 直接注入原生 JSON 定义的 functions
                .withJson(new StringReader("{ \"functions\": " + functionsJson + " }"))
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Multiply)
        ));
    }
    /**
     * 构建高亮配置
     */
    private Highlight buildHighlight() {
        return Highlight.of(h -> h
                .fields("title", HighlightField.of(hf -> hf
                        .preTags("<em class=\"highlight\">")
                        .postTags("</em>")
                        .numberOfFragments(0) // 返回整个字段
                ))
                .fields("description", HighlightField.of(hf -> hf
                        .preTags("<em class=\"highlight\">")
                        .postTags("</em>")
                        .fragmentSize(200) // 片段大小
                        .numberOfFragments(1)
                ))
        );
    }

    /**
     * 解析搜索结果
     */
    private IPage<VideoDTO> parseSearchResponse(SearchResponse<VideoDTO> response, VideoQueryParam param) {
        List<VideoDTO> records = new ArrayList<>();
        
        for (Hit<VideoDTO> hit : response.hits().hits()) {
            VideoDTO video = hit.source();
            if (video == null) continue;
            
            // 1. 应用高亮结果
            Map<String, List<String>> highlights = hit.highlight();
            if (highlights != null) {
                if (highlights.containsKey("title") && !highlights.get("title").isEmpty()) {
                    video.setTitle(highlights.get("title").get(0));
                }
                if (highlights.containsKey("description") && !highlights.get("description").isEmpty()) {
                    video.setDescription(highlights.get("description").get(0));
                }
            }
            
            // 2. 设置ES评分（可选，用于调试）
            if (hit.score() != null) {
                video.setEsScore(hit.score());
            }
            
            records.add(video);
        }
        
        // 构建分页结果
        Page<VideoDTO> page = new Page<>(param.getCurrent(), param.getSize());
        page.setRecords(records);
        page.setTotal(response.hits().total() != null ? response.hits().total().value() : 0);
        
        return page;
    }
}
