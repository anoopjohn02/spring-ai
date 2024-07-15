package com.anoop.ai.services;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

@AllArgsConstructor
public class OpenAIServiceImpl implements OpenAIService{

    private final ChatClient chatClient;

    @Override
    public String answer(String question) {
        PromptTemplate promptTemplate = new PromptTemplate(question);
        Prompt prompt = promptTemplate.create();

        ChatResponse response = chatClient.call(prompt);

        return response.getResult().getOutput().getContent();
    }
}
