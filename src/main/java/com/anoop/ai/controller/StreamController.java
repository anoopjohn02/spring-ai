package com.anoop.ai.controller;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.services.OpenAIService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "/v1/stream")
public class StreamController {

    private final OpenAIService openAIService;

    @PostMapping(value = "/api", produces = MediaType.TEXT_EVENT_STREAM_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<AIChatMessage> stream(@RequestBody AIChatMessage aiChatMessage) {
        log.info("Sending message");
        return openAIService.streamingChatApi(aiChatMessage);
    }

    @GetMapping(value = "/temp", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> tempStream() {
        log.info("Temp Streaming...");
        return Flux.range(1, 10)
                .delayElements(Duration.ofSeconds(1))
                .map(i -> "stream message-"+i +" ");
    }
}
