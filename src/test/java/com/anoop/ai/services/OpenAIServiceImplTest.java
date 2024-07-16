package com.anoop.ai.services;

import com.anoop.ai.model.Answer;
import com.anoop.ai.model.Question;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class OpenAIServiceImplTest {

    @Autowired
    OpenAIService openAIService;

    @Test
    void testAnswer() {
        Answer answer = openAIService.answer(new Question("Tell me a dad joke."));
        log.info("Answer: {}", answer.answer());
    }
}