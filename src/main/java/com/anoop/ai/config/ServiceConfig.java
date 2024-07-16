package com.anoop.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ServiceConfig {
    private final ChatClient.Builder chatClientBuilder;

    @Bean
    public ChatClient chatClient() {
        return chatClientBuilder.build();
    }
}
