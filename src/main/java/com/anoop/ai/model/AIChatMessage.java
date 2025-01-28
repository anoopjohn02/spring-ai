package com.anoop.ai.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record AIChatMessage(UUID messageId, String content, String sender, MessageType type) {
}
