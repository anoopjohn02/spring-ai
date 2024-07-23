package com.anoop.ai.services;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.model.Answer;
import com.anoop.ai.model.CapitalRequest;
import com.anoop.ai.model.MessageType;
import com.anoop.ai.model.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
        UUID messageId = UUID.randomUUID();
        Flux<String> flux = chatClient.prompt()
                .user(message.content())
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, message.sender())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
                        .param(FILTER_EXPRESSION, "userId == '"+message.sender()+"'")
                        )
                .stream()
                .content();
        flux.subscribe(token -> tokenReceived(message.sender(), messageId, token));
    }

    private void tokenReceived(String userId, UUID messageId, String token) {
        log.debug(token);
        AIChatMessage message = AIChatMessage.builder()
                .messageId(messageId)
                .content(token)
                .sender("AI")
                .type(MessageType.CHAT)
                .build();
        send(userId, message);
    }

    private void send(String userId, AIChatMessage message){
        String destination = WS_MESSAGE_DESTINATION + "/" + userId;
        simpMessagingTemplate.convertAndSend(destination, message);
    }

}
