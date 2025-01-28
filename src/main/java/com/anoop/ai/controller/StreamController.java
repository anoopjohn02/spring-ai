package com.anoop.ai.controller;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.model.MessageType;
import com.anoop.ai.model.ResponseMessage;
import com.anoop.ai.services.OpenAIService;
import java.time.Duration;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "/v1/stream")
public class StreamController {

  private final OpenAIService openAIService;

  @PostMapping(
      value = "/api",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public Flux<AIChatMessage> stream(@RequestBody AIChatMessage aiChatMessage) {
    log.info("Sending message");
    try{
      return openAIService.streamingChatApi(aiChatMessage);
    } catch (Exception e) {
      log.error("Error: ", e);
      throw e;
    }
  }

  @GetMapping(value = "/temp", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<AIChatMessage> tempStream() {
    log.info("Temp Streaming...");
    return Flux.range(1, 10)
        .delayElements(Duration.ofSeconds(1))
        .map(i -> aIChatMessage("stream message-" + i + " "));
  }

  private AIChatMessage aIChatMessage(String message) {
    return AIChatMessage.builder()
        .messageId(UUID.randomUUID())
        .content(message)
        .sender("AI")
        .type(MessageType.CHAT)
        .build();
  }
}
