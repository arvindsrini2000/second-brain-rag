package com.secondbrain.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@Service
public class AgentService {

    private final StreamingChatModel streamingChatModel;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> vectorStore;
    private final ChatMemoryStore chatMemoryStore;

    public AgentService(StreamingChatModel streamingChatModel, 
                        EmbeddingModel embeddingModel, 
                        EmbeddingStore<TextSegment> vectorStore,
                        ChatMemoryStore chatMemoryStore) {
        this.streamingChatModel = streamingChatModel;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
        this.chatMemoryStore = chatMemoryStore;
        
        // this.chatMemory.add(SystemMessage.from(
        //     "You are a highly intelligent enterprise assistant. " +
        //     "Answer the user's questions based ONLY on the provided context. " +
        //     "If the answer is not in the context, say 'I don't have enough information.'"
        // ));
    }

public ResponseBodyEmitter askQuestionStream(String sessionId, String userQuestion) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(120000L); 

        // 1. Initialize the Memory Manager for this specific session
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(sessionId)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();

        // 2. Add the CLEAN question to memory (this automatically saves to Postgres)
        chatMemory.add(UserMessage.from(userQuestion));

        // 3. Perform the Vector Search
        Embedding queryEmbedding = embeddingModel.embed(userQuestion).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(3)
                .build();
        
        EmbeddingSearchResult<TextSegment> searchResult = vectorStore.search(searchRequest);
        StringBuilder contextBuilder = new StringBuilder();
        for (EmbeddingMatch<TextSegment> match : searchResult.matches()) {
            contextBuilder.append(match.embedded().text()).append("\n\n");
        }

        String enrichedQuestion = "Context:\n" + contextBuilder.toString() + "\nQuestion: " + userQuestion;

        // 4. Create a temporary copy of the chat history just for this LLM call
        List<ChatMessage> messagesForLlm = new java.util.ArrayList<>(chatMemory.messages());
        
        // 5. Swap the last message (the clean question) with the enriched one
        messagesForLlm.set(messagesForLlm.size() - 1, UserMessage.from(enrichedQuestion));

        // 6. Send the temporary list to the LLM
        streamingChatModel.chat(messagesForLlm, new StreamingChatResponseHandler() {
            
            @Override
            public void onPartialResponse(String partialResponse) {
                try {
                    emitter.send(partialResponse);
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                // 7. Add the AI's response to memory (this automatically saves to Postgres)
                chatMemory.add(completeResponse.aiMessage());
                emitter.complete(); 
            }

            @Override
            public void onError(Throwable error) {
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }
}