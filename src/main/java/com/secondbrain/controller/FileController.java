package com.secondbrain.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class FileController {

    private final JdbcTemplate jdbcTemplate;

    // Spring Boot automatically injects the JdbcTemplate
    public FileController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/files")
    public List<String> getUploadedFiles() {
        // Query Postgres for all unique file_name values inside the JSON metadata
        String sql = "SELECT DISTINCT metadata->>'file_name' FROM document_chunks WHERE metadata->>'file_name' IS NOT NULL";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}