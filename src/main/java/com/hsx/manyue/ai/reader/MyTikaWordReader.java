package com.hsx.manyue.ai.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Word 文件读取
 **/
@Component
public class MyTikaWordReader {

    @Value("classpath:/document/55f79946a0964b89bc7ab9b55e4a49ff.docx")
    private Resource resource;

    public List<Document> loadWord() {
        // 新建 TikaDocumentReader 阅读器
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
        // 读取并转换为 Document 文档集合
        List<Document> documents = tikaDocumentReader.get();

        // 文档分块
        TokenTextSplitter splitter = new TokenTextSplitter(); // 不设置任何构造参数，表示使用默认设置
        return splitter.apply(documents);
    }
}
