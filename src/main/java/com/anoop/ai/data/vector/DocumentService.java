package com.anoop.ai.data.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final VectorStore vectorStore;
    private final PdfDocumentReaderConfig pdfDocumentReaderConfig;
    private final TokenTextSplitter tokenTextSplitter;
    @Async
    public void loadSingleDocument(Resource resource, String userId) {

        DocumentReader documentReader = new PagePdfDocumentReader(resource, pdfDocumentReaderConfig);
        List<Document> documents = documentReader.get();

        documents.forEach(document -> document.getMetadata().put("userId", userId));

        log.info("Splitting the documents");
        List<Document> chunks = tokenTextSplitter.split(documents);

        log.info("Storing chunks to vector db");
        vectorStore.add(chunks);
    }
}
