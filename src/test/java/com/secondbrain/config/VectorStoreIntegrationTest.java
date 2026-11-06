package com.secondbrain.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class VectorStoreIntegrationTest {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Test
    void shouldSaveAndSearchEmbeddingsInPostgres() {
        // 1. Create a chunk of text
        TextSegment segment = TextSegment.from("The architectural decision uses PostgreSQL and pgvector for hybrid search.");
        
        // 2. Generate the 384-dimensional vector
        Embedding embedding = embeddingModel.embed(segment).content();
        
        // 3. Save both the text and the vector into the database
        embeddingStore.add(embedding, segment);

        // 4. Perform a Semantic Search using the new 1.20.x API
        String query = "What database is this project using?";
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        
        // Build the search request
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(1)
                .build();
                
        // Execute the search
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> results = searchResult.matches();

        // 5. Verify the result
        assertFalse(results.isEmpty(), "Should find a matching document in Postgres");
        
        TextSegment matchedSegment = results.get(0).embedded();
        System.out.println("Semantic Search returned: " + matchedSegment.text());
        
        assertEquals(segment.text(), matchedSegment.text(), "The retrieved text should match the stored text");
    }
}