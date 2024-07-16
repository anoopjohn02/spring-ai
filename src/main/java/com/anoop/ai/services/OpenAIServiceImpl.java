package com.anoop.ai.services;

import com.anoop.ai.model.Answer;
import com.anoop.ai.model.CapitalRequest;
import com.anoop.ai.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIServiceImpl implements OpenAIService{

    private final ChatClient.Builder chatClientBuilder;

    @Value("classpath:templates/capital-with-info.st")
    private Resource capitalPromptWithInfo;

    @Override
    public Answer answer(Question question) {
        ChatClient chatClient = chatClientBuilder.build();
        return new Answer(chatClient.prompt().user(question.getQuestion()).call().content());
    }

    @Override
    public Answer getCapitalWithInfo(CapitalRequest getCapitalRequest) {
        ChatClient chatClient = chatClientBuilder.build();
        PromptTemplate promptTemplate = new PromptTemplate(capitalPromptWithInfo);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", getCapitalRequest.getRegion()));
        return new Answer(chatClient.prompt(prompt).call().content());
    }

}
