package com.hsx.manyue.ai.controller;

import com.hsx.manyue.ai.reader.*;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 文档读取与解析的演示控制器。
 * 该控制器提供了一系列接口，用于展示如何从不同格式的源文件（如 txt, json, md, html, pdf, word）
 * 中读取内容，并将其转换为 Spring AI 标准的 Document 对象。
 * 这是构建 RAG 知识库，进行文档向量化前的关键一步。
 **/
@RestController
@RequestMapping("/read")
public class ReaderController {

    @Resource
    private MyTextReader textReader;

    @Resource
    private MyJsonReader jsonReader;

    @Resource
    private MyMarkdownReader markdownReader;

    @Resource
    private MyHtmlReader htmlReader;

    @Resource
    private MyPdfReader pdfReader;

    @Resource
    private MyTikaWordReader tikaWordReader;

    @GetMapping(value = "/txt")
    public List<Document> readText() {
        return textReader.loadText();
    }

    @GetMapping(value = "/txt2")
    public List<Document> readText2() {
        return textReader.loadTextAndSplit();
    }

    @GetMapping(value = "/json")
    public List<Document> readJson() {
        return jsonReader.loadJson();
    }

    @GetMapping(value = "/md")
    public List<Document> readMarkdown() {
        return markdownReader.loadMarkdown();
    }

    @GetMapping(value = "/html")
    public List<Document> readHtml() {
        return htmlReader.loadHtml();
    }

    @GetMapping(value = "/pdf")
    public List<Document> readPdf() {
        return pdfReader.getDocsFromPdf();
    }

    @GetMapping(value = "/word")
    public List<Document> readWord() {
        return tikaWordReader.loadWord();
    }

}
