package com.anoop.ai.controller;

import com.anoop.ai.services.OpenAIService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class ChatController {
    private final OpenAIService openAIService;

    @GetMapping("/ai")
    String generation(String userInput) {
        return openAIService.answer(userInput);
    }
}
