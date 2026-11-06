package com.secondbrain.model;

import java.util.List;

/**
 * Represents a cleanly extracted piece of a document.
 * 
 * @param text        The actual content (e.g., a paragraph).
 * @param breadcrumbs The header hierarchy (e.g., ["Chapter 1", "Introduction"]).
 * @param sourcePath  Where this chunk came from (e.g., "docs/architecture.md").
 */
public record DocumentChunk(
    String text,
    List<String> breadcrumbs,
    String sourcePath
) {}