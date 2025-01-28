package com.anoop.ai.services;

import com.anoop.ai.model.AIChatMessage;
import com.anoop.ai.model.Answer;
import com.anoop.ai.model.Question;
import reactor.core.publisher.Flux;

public interface OpenAIService {
    Answer answer(Question question);

    void streamingChat(AIChatMessage message);
    Flux<AIChatMessage> streamingChatApi(AIChatMessage message);
}
