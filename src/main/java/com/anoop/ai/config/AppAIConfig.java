package com.anoop.ai.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppAIConfig {
    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemory chatMemory;
    private final VectorStore vectorStore;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Bean
    public ChatClient chatClient() {
        return chatClientBuilder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults())
                )
                .build();
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    /*@Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel(new OpenAiApi(apiKey));
    }*/
}
