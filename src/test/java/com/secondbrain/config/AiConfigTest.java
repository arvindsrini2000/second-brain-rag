package com.secondbrain.config;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AiConfigTest {

    @Test
    void shouldGenerateEmbeddingsLocally() {
        // 1. Manually instantiate the model for testing
        EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();
        
        // 2. Embed a piece of text
        Embedding embedding = model.embed("Hello, Second Brain!").content();
        
        // 3. Verify the AI gave us a real vector
        assertNotNull(embedding);
        
        // The all-MiniLM-L6-v2 model always compresses text into exactly 384 dimensions
        assertEquals(384, embedding.dimension(), "The vector should have exactly 384 dimensions");
        
        System.out.println("AI Vector successfully generated! First 5 values: ");
        for (int i = 0; i < 5; i++) {
            System.out.println(embedding.vector()[i]);
        }
    }
}