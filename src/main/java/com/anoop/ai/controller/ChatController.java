package com.anoop.ai.controller;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.services.OpenAIService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

    @PostMapping("/api/stream")
    public ResponseEntity<StreamingResponseBody> stream(@RequestBody AIChatMessage aiChatMessage) {
        StreamingResponseBody stream = out ->
            openAIService.streamingChatApi(aiChatMessage, out);
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, List.of("text/event-stream"));
        return new ResponseEntity(stream, headers, HttpStatus.OK);
    }

    @PostMapping("/temp/stream")
    public ResponseEntity<StreamingResponseBody> tempStream() {
        StreamingResponseBody stream = out -> {
            for(int i=0; i< 10; i++) {
                String message = "stream message-"+i +" ";
                log.info("Sending {}", message);
                out.write(message.getBytes());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, List.of("text/event-stream"));
        return new ResponseEntity(stream, headers, HttpStatus.OK);
    }
}