package com.anoop.ai.services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIServiceImpl implements OpenAIService{

    private final ChatClient.Builder chatClientBuilder;

    @Override
    public String answer(String question) {
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt().user(question).call().content();
    }
}
