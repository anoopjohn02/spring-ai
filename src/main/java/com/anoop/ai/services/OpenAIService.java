package com.anoop.ai.services;

import com.anoop.ai.model.Answer;
import com.anoop.ai.model.CapitalRequest;
import com.anoop.ai.model.Question;

public interface OpenAIService {
    Answer answer(Question question);
    Answer getCapitalWithInfo(CapitalRequest capitalRequest);
}
