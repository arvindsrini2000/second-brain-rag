package com.secondbrain.controller;

import com.secondbrain.service.AgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AgentService agentService;

    public ChatController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/ask")
    public ResponseBodyEmitter ask(@RequestParam String question) {
        // Return the stream instead of a blocked string
        return agentService.askQuestionStream("arvind-local-session", question); 
    }
}