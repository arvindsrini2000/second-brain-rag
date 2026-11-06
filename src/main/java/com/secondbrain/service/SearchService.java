package com.secondbrain.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> vectorStore;

    // Constructor Injection again! 
    // Spring plugs in the exact same AI model and DB connection we used for ingestion.
    public SearchService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> vectorStore) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    public void searchAndPrint(String userQuery) {
        System.out.println("\n--- Searching for: '" + userQuery + "' ---");

        // 1. Convert the user's question into a 384-dimensional vector
        Embedding queryEmbedding = embeddingModel.embed(userQuery).content();

        // 2. Build the search request (asking for the top 3 most relevant chunks)
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .build();

        // 3. Execute the search in PostgreSQL
        EmbeddingSearchResult<TextSegment> searchResult = vectorStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        // 4. Print the results
        if (matches.isEmpty()) {
            System.out.println("No relevant information found.");
            return;
        }

        for (int i = 0; i < matches.size(); i++) {
            EmbeddingMatch<TextSegment> match = matches.get(i);
            // match.score() tells us how confident the AI is (0.0 to 1.0)
            System.out.printf("Result %d (Score: %.4f):%n%s%n%n", 
                    i + 1, match.score(), match.embedded().text());
        }
    }
}