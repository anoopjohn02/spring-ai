package com.anoop.ai.controller;

import com.anoop.ai.model.Answer;
import com.anoop.ai.model.CapitalRequest;
import com.anoop.ai.model.Question;
import com.anoop.ai.services.OpenAIService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/v1/ai")
public class ChatController {
    private final OpenAIService openAIService;

    @PostMapping("/chat")
    public Answer generation(@RequestBody Question question) {
        return openAIService.answer(question);
    }

    @PostMapping("/capital/info")
    public Answer capitalInfo(@RequestBody CapitalRequest capitalRequest) {
        return openAIService.getCapitalWithInfo(capitalRequest);
    }
}
