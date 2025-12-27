package com.hsx.manyue.ai.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PDF 文件读取
 **/
@Component
public class MyPdfReader {

    public List<Document> getDocsFromPdf() {
        // 新建 PagePdfDocumentReader 阅读器
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/document/profile.pdf", // PDF 文件路径
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0) // 设置页面顶边距为0
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0) // 设置删除顶部文本行数为0
                                .build())
                        .withPagesPerDocument(1) // 设置每个文档包含1页
                        .build());

        // 读取并转换为 Document 文档集合
        return pdfReader.read();
    }
}