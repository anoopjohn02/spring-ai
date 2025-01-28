package com.anoop.ai.services;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor.FILTER_EXPRESSION;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.model.Answer;
import com.anoop.ai.model.MessageType;
import com.anoop.ai.model.Question;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIAgent implements Agent {

  private final ChatClient chatClient;

  @Value("classpath:templates/greet-me.st")
  private Resource greet;

  @Override
  public Flux<AIChatMessage> streamingChatApi(AIChatMessage message) {
    UUID messageId = UUID.randomUUID();
    return ask(message).map(s -> this.aiChatMessage(messageId, s));
  }

  private Flux<String> ask(AIChatMessage message) {
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(greet);
    Prompt prompt = systemPromptTemplate.create(Map.of("name", message.sender()));
    return chatClient
        .prompt()
        .system(prompt.getContents())
        .user(message.content())
        .advisors(
            a ->
                a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, message.sender())
                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
                    .param(FILTER_EXPRESSION, "userId == '" + message.sender() + "'"))
        .stream()
        .content();
  }

  private AIChatMessage aiChatMessage(UUID messageId, String token) {
    log.info("Token received: {}", token);
    return AIChatMessage.builder()
        .messageId(messageId)
        .content(token)
        .sender("AI")
        .type(MessageType.CHAT)
        .build();
  }

}
