package com.secondbrain.config;

import dev.langchain4j.model.chat.StreamingChatModel; // <-- NEW
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel; // <-- NEW
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    // Replace the old ChatModel with StreamingChatModel
    @Bean
    public StreamingChatModel streamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2")
                .temperature(0.0)
                .timeout(Duration.ofSeconds(120))
                .build();
    }
}