package com.anoop.ai.controller;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.services.OpenAIService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Controller
public class ChatController {

    private final OpenAIService openAIService;

    @MessageMapping("/chat.register")
    @SendTo("/topic/public")
    public AIChatMessage register(@Payload AIChatMessage AIChatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", AIChatMessage.sender());
        return AIChatMessage;
    }

    @MessageMapping("/chat.send/{sender}")
    @SendTo("/topic/{sender}")
    public AIChatMessage sendMessage(@Payload AIChatMessage aiChatMessage) {
        openAIService.streamingChat(aiChatMessage);
        return aiChatMessage;
    }

    @PostMapping(value = "/api/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AIChatMessage> stream(@RequestBody AIChatMessage aiChatMessage) {
        return openAIService.streamingChatApi(aiChatMessage);
    }

    @GetMapping(value = "/temp/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> tempStream() {
        return Flux.range(1, 10)
                .delayElements(Duration.ofSeconds(1))
                .map(i -> "stream message-"+i +" ");
    }
}