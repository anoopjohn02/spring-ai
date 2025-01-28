package com.anoop.ai.services;

import com.anoop.ai.model.AIChatMessage;
import reactor.core.publisher.Flux;

public interface Agent {

  Flux<AIChatMessage> streamingChatApi(AIChatMessage message);
}
