package com.anoop.ai.controller;

import com.anoop.ai.model.Answer;
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
        return new Answer(openAIService.answer(question.getQuestion()));
    }
}
