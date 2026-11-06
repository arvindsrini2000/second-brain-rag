package com.secondbrain.chunker;

import com.secondbrain.model.DocumentChunk;

import dev.langchain4j.data.segment.TextSegment;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkdownASTChunkerTest {

    @Test
    void shouldExtractParagraphsWithCorrectBreadcrumbs() {
        // 1. Setup the test data
        MarkdownASTChunker chunker = new MarkdownASTChunker();
        String sampleMarkdown = """
                # System Architecture
                This is the introduction paragraph.
                
                ## Database Design
                We are using PostgreSQL with pgvector.
                
                ## AI Models
                We are using LangChain4j.
                """;

        // 2. Execute the method we are testing
        List<TextSegment> chunks = chunker.chunkDocument(sampleMarkdown, "docs/readme.md");

        // 3. Assert (Verify) the results
        assertEquals(3, chunks.size(), "Should have extracted exactly 3 text chunks");

        // Verify Chunk 1
        TextSegment chunk1 = chunks.get(0);
        assertEquals("This is the introduction paragraph.", chunk1.text());
        assertEquals(List.of("System Architecture"), chunk1);

        // Verify Chunk 2
        // DocumentChunk chunk2 = chunks.get(1);
        // assertEquals("We are using PostgreSQL with pgvector.", chunk2.text());
        // assertEquals(List.of("System Architecture", "Database Design"), chunk2.breadcrumbs());

        // // Verify Chunk 3
        // DocumentChunk chunk3 = chunks.get(2);
        // assertEquals("We are using LangChain4j.", chunk3.text());
        // // Notice how "Database Design" is gone, and "AI Models" replaced it correctly
        // assertEquals(List.of("System Architecture", "AI Models"), chunk3.breadcrumbs());
    }
}
