package com.secondbrain.service;

import com.secondbrain.chunker.MarkdownASTChunker;
import com.secondbrain.chunker.PdfChunker;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class IngestionService {

    private final MarkdownASTChunker markdownChunker;
    private final PdfChunker pdfChunker;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> vectorStore;

    // Constructor Injection: Spring will automatically plug in the instances
    // we built in AiConfig and VectorStoreConfig.
    public IngestionService(EmbeddingStore<TextSegment> vectorStore,
            EmbeddingModel embeddingModel,
            MarkdownASTChunker markdownChunker,
            PdfChunker pdfChunker) {
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.markdownChunker = markdownChunker;
        this.pdfChunker = pdfChunker;
    }

    public void ingestPdfDocument(byte[] pdfBytes, String filename) {
        System.out.println("Extracting text from PDF: " + filename);

        DocumentParser parser = new ApachePdfBoxDocumentParser();
        Document document = parser.parse(new ByteArrayInputStream(pdfBytes));

        // 1. Inject the filename into the parsed document's metadata
        document.metadata().put("file_name", filename);

        // 2. The Recursive Chunker copies it to all overlapping segments
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        vectorStore.addAll(embeddings, segments);

        System.out.println("Successfully ingested PDF: " + segments.size() + " chunks.");
    }

    public void ingestMarkdownDocument(String markdownContent, String sourcePath) {
        List<TextSegment> segments = markdownChunker.chunkDocument(markdownContent, sourcePath);
        vectorStore.addAll(embeddingModel.embedAll(segments).content(), segments);
    }
}