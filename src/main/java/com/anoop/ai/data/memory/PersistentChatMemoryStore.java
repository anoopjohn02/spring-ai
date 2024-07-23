package com.anoop.ai.data.memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersistentChatMemoryStore implements ChatMemory {

    Map<Object, List<Message>> inMemoryStore = new HashMap<>();

    @Override
    public void add(String conversationId, Message message) {
        List<Message> existingOrNew = inMemoryStore.getOrDefault(conversationId, new ArrayList<>());
        existingOrNew.add(message);
        inMemoryStore.put(conversationId, existingOrNew);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if(inMemoryStore.containsKey(conversationId)) {
            inMemoryStore.get(conversationId).addAll(messages);
        } else {
            inMemoryStore.put(conversationId, messages);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        return inMemoryStore.getOrDefault(conversationId, new ArrayList<>());
    }

    @Override
    public void clear(String conversationId) {
        inMemoryStore.remove(conversationId);
    }
}
