package com.secondbrain.controller;

import com.secondbrain.service.IngestionService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/admin")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return "Error: Please select a file to upload.";
            }

            String filename = file.getOriginalFilename();

            // Route PDF files to the new parser
            if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
                ingestionService.ingestPdfDocument(file.getBytes(), filename);
            }
            // Route Markdown and TXT files to your AST parser
            else {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                ingestionService.ingestMarkdownDocument(content, filename);
            }

            return "Success: " + filename + " was successfully chunked and embedded into PostgreSQL.";
        } catch (Exception e) {
            return "Error during ingestion: " + e.getMessage();
        }
    }

    // A secure endpoint to manually trigger a re-index of the file
    @PostMapping("/ingest")
    public String ingestDefaultFile() {
        try {
            Path filePath = Path.of("data/java-notes.md");
            if (!Files.exists(filePath)) {
                return "Error: File not found at " + filePath.toAbsolutePath();
            }

            String content = Files.readString(filePath);
            ingestionService.ingestMarkdownDocument(content, filePath.toString());

            return "Success: Document successfully chunked and embedded into PostgreSQL.";
        } catch (Exception e) {
            return "Error during ingestion: " + e.getMessage();
        }
    }
}