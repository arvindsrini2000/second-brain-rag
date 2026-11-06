package com.secondbrain.chunker;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Component
public class MarkdownASTChunker {

    private final Parser parser;

    public MarkdownASTChunker() {
        this.parser = Parser.builder().build();
    }

    /**
     * Parses markdown text and converts it directly into LangChain4j TextSegments with metadata.
     */
    public List<TextSegment> chunkDocument(String markdown, String sourcePath) {
        Node document = parser.parse(markdown);
        ChunkingVisitor visitor = new ChunkingVisitor(sourcePath);
        document.accept(visitor);
        return visitor.getSegments();
    }

    private static class ChunkingVisitor extends AbstractVisitor {
        private final String sourcePath;
        // Changed to hold LangChain4j TextSegments directly
        private final List<TextSegment> segments = new ArrayList<>();
        
        private final Stack<String> headerBreadcrumbs = new Stack<>();
        private int currentHeaderLevel = 0;

        public ChunkingVisitor(String sourcePath) {
            this.sourcePath = sourcePath;
        }

        public List<TextSegment> getSegments() {
            return segments;
        }

        @Override
        public void visit(Heading heading) {
            String headingText = extractText(heading);
            
            while (!headerBreadcrumbs.isEmpty() && heading.getLevel() <= currentHeaderLevel) {
                headerBreadcrumbs.pop();
                currentHeaderLevel--;
            }
            
            headerBreadcrumbs.push(headingText);
            currentHeaderLevel = heading.getLevel();
            
            visitChildren(heading);
        }

        @Override
        public void visit(Paragraph paragraph) {
            String text = extractText(paragraph);
            if (!text.isBlank()) {
                // 1. Build the context-rich string
                String enrichedText = String.join(" > ", headerBreadcrumbs) + "\n" + text.trim();
                
                // 2. Create metadata with the filename
                Metadata metadata = Metadata.from("file_name", sourcePath);
                
                // 3. Bake both into a ready-to-save TextSegment
                segments.add(TextSegment.from(enrichedText, metadata));
            }
            visitChildren(paragraph);
        }

        private String extractText(Node node) {
            StringBuilder sb = new StringBuilder();
            node.accept(new AbstractVisitor() {
                @Override
                public void visit(Text text) {
                    sb.append(text.getLiteral());
                }
            });
            return sb.toString();
        }
    }
}