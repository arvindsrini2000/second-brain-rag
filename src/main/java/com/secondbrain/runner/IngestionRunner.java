package com.secondbrain.runner;

import com.secondbrain.service.AgentService;
import com.secondbrain.service.IngestionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@ConditionalOnProperty(name = "run.ingestion", havingValue = "true")
public class IngestionRunner implements CommandLineRunner {

    private final IngestionService ingestionService;
    private final AgentService agentService;

    // We now inject BOTH services
    public IngestionRunner(IngestionService ingestionService, AgentService agentService) {
        this.ingestionService = ingestionService;
        this.agentService = agentService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Starting Second Brain Initializer ===");
        
        Path filePath = Path.of("data/java-notes.md");
        
        if (Files.exists(filePath)) {
            String content = Files.readString(filePath);
            ingestionService.ingestMarkdownDocument(content, filePath.toString());
            System.out.println("=== Initialization Complete ===\n");
            
            // --- NEW CODE: Test the Semantic Search ---
            // Even though the exact word "threads" isn't in the question, 
            // the AI should understand the meaning and find the Virtual Threads chunk!
            //agentService.askQuestion("How does Java 25 handle high-throughput concurrency?");
            
        } else {
            System.out.println("Sample file not found at: " + filePath.toAbsolutePath());
        }
    }
}