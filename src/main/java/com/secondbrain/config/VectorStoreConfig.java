package com.secondbrain.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

    @Bean
    public EmbeddingStore<TextSegment> pgVectorStore() {
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("secondbrain")
                .user("postgres")
                .password("postgres")
                .table("document_chunks")
                .dimension(384) // Must match our all-MiniLM-L6-v2 output
                .dropTableFirst(true) // Great for early development, resets data on boot
                .build();
    }
}