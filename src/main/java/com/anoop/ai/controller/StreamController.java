package com.anoop.ai.controller;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.services.Agent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "/v1/stream")
public class StreamController {

  private final Agent agent;

  @PostMapping(
      value = "/api",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public Flux<AIChatMessage> stream(@RequestBody AIChatMessage aiChatMessage) {
    log.info("Sending message");
    try {
      return agent.streamingChatApi(aiChatMessage);
    } catch (Exception e) {
      log.error("Error: ", e);
      throw e;
    }
  }
}
