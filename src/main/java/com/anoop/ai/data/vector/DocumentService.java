package com.anoop.ai.data.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;
    private final TokenTextSplitter tokenTextSplitter;
    @Async
    public void loadSingleDocument(Resource resource, String userId) {
        var textReader = new TextReader(resource);
        textReader.setCharset(Charset.defaultCharset());
        List<Document> documents = textReader.get();
        documents.forEach(document -> document.getMetadata().put("userId", userId));

        log.info("Splitting the documents");
        List<Document> chunks = tokenTextSplitter.split(documents);

        log.info("Storing chunks to vector db");
        vectorStore.add(chunks);
    }
}
