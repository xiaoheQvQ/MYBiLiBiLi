package com.hsx.manyue.ai.runner;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 将文档通过向量模型，向量化存储到数据库中
 **/
@Component
public class InitEmbeddingIndexRunner implements ApplicationRunner {

    @Resource
    private VectorStore vectorStore;

    @Override
    public void run(ApplicationArguments args) {
        // 新建 PagePdfDocumentReader 阅读器
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/document/profile_c.pdf", // 类路径PDF文件
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0) // 设置页面顶边距为0
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0) // 设置删除顶部文本行数为0
                                .build())
                        .withPagesPerDocument(1) // 设置每个文档包含1页
                        .build());

        // 读取并转换为 Document 文档集合
        List<Document> documents = pdfReader.get();

        // 防止重复添加到 Redis 中
        for (Document document : documents) {
            // 从向量数据中，查询当前文档
            List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(document.getText())
                    .topK(1) // 查询一条最高得分的
                    .build());

            // 如果结果不为空，并且得分大于 0.99，则表示文档较高几率重复，直接跳过
            if (!results.isEmpty() && results.get(0).getScore() > 0.99)
                continue;

            // 通过向量模型，将文档向量化存储到 Redis 中
            vectorStore.add(List.of(document));
        }
    }
}