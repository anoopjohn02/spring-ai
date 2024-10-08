package com.anoop.ai.services;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.model.Answer;
import com.anoop.ai.model.CapitalRequest;
import com.anoop.ai.model.MessageType;
import com.anoop.ai.model.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor.FILTER_EXPRESSION;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIServiceImpl implements OpenAIService{

    private final ChatClient chatClient;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private static final String WS_MESSAGE_DESTINATION = "/topic";

    @Value("classpath:templates/capital-with-info.st")
    private Resource capitalPromptWithInfo;

    @Value("classpath:templates/greet-me.st")
    private Resource greet;

    @Override
    public Answer answer(Question question) {
        return Answer.builder()
                .answer(chatClient.prompt().user(question.question()).call().content())
                .build();
    }

    @Override
    public Answer getCapitalWithInfo(CapitalRequest getCapitalRequest) {
        PromptTemplate promptTemplate = new PromptTemplate(capitalPromptWithInfo);
        Prompt prompt = promptTemplate.create(Map.of("stateOrCountry", getCapitalRequest.region()));
        return new Answer(chatClient.prompt(prompt).call().content());
    }

    @Async
    @Override
    public void streamingChat(AIChatMessage message) {
        Flux<String> flux = ask(message);
        UUID messageId = UUID.randomUUID();
        flux.subscribe(token -> tokenReceived(message.sender(), messageId, token));
    }

    @Override
    public void streamingChatApi(AIChatMessage message, OutputStream out) {
        Flux<String> flux = ask(message);
        UUID messageId = UUID.randomUUID();
        flux.subscribe(token -> writeToStream(out, messageId, token));
    }

    private Flux<String> ask(AIChatMessage message) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(greet);
        Prompt prompt = systemPromptTemplate.create(Map.of("name", message.sender()));
        return chatClient.prompt()
                .system(prompt.getContents())
                .user(message.content())
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, message.sender())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
                        .param(FILTER_EXPRESSION, "userId == '"+message.sender()+"'")
                )
                .stream()
                .content();
    }

    private AIChatMessage aiChatMessage(UUID messageId, String token) {
        return AIChatMessage.builder()
                .messageId(messageId)
                .content(token)
                .sender("AI")
                .type(MessageType.CHAT)
                .build();
    }

    private void writeToStream(OutputStream out, UUID messageId, String token) {
        log.info(token);
        AIChatMessage message = aiChatMessage(messageId, token);
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsBytes(message));
        } catch (IOException e) {
            log.error("Exception while write to stream ", e);
            throw new RuntimeException(e);
        }
    }

    private void tokenReceived(String userId, UUID messageId, String token) {
        log.debug(token);
        send(userId, aiChatMessage(messageId, token));
    }

    private void send(String userId, AIChatMessage message){
        String destination = WS_MESSAGE_DESTINATION + "/" + userId;
        simpMessagingTemplate.convertAndSend(destination, message);
    }

}
