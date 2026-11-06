package com.secondbrain.chunker;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;

@Component
public class PdfChunker {

    public List<TextSegment> chunkDocument(byte[] pdfBytes, String sourcePath) {
        // 1. Parse the binary PDF into a single LangChain4j Document
        DocumentParser parser = new ApachePdfBoxDocumentParser();
        Document document = parser.parse(new ByteArrayInputStream(pdfBytes));

        // 2. Attach the metadata to the parent Document
        document.metadata().put("file_name", sourcePath);

        // 3. Split it (the recursive splitter copies the metadata to every chunk)
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        return splitter.split(document);
    }
}