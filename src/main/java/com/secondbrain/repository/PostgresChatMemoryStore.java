package com.secondbrain.repository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

// LangChain4j's built-in JSON converters
import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

@Repository
public class PostgresChatMemoryStore implements ChatMemoryStore {

    private final JdbcTemplate jdbcTemplate;

    public PostgresChatMemoryStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initTable() {
        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS chat_memory (" +
            "memory_id VARCHAR(255) PRIMARY KEY, " +
            "messages JSONB)"
        );
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String sql = "SELECT messages FROM chat_memory WHERE memory_id = ?";
        List<String> jsonMessages = jdbcTemplate.query(sql, 
            (rs, rowNum) -> rs.getString("messages"), 
            (String) memoryId
        );
        
        if (jsonMessages.isEmpty() || jsonMessages.get(0) == null) {
            return new ArrayList<>();
        }
        return messagesFromJson(jsonMessages.get(0));
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = messagesToJson(messages);
        
        // UPSERT: Insert new row or update existing row if memory_id matches
        String sql = "INSERT INTO chat_memory (memory_id, messages) VALUES (?, ?::jsonb) " +
        "ON CONFLICT (memory_id) DO UPDATE SET messages = EXCLUDED.messages";
        jdbcTemplate.update(sql, (String) memoryId, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String sql = "DELETE FROM chat_memory WHERE memory_id = ?";
        jdbcTemplate.update(sql, (String) memoryId);
    }
}