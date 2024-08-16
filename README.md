# Generative AI development in Java
This project contains the codes to build an application that support AI chatbot in the context of uploaded pdf file.

Technologies:
- Java 17+
- Spring boot
- Spring AI
- Spring web flux
- Websocket

AI Integrations:
- OpenAPI for LLM
- Chroma db for Vector store
- In-memory for memory
- Webflux for streaming

# Run chroma db

Run chroma db docker container in local before you start.

    docker run -p 8000:8000 chromadb/chroma

# Environment variable needs to set

| name                   | value                 | 
|------------------------|-----------------------|
| OPEN_API_KEY           |                       | 
| spring.profiles.active | chroma                | 

# Steps

1. Open code in IDE and run locally
2. Upload document
    - Use postman to upload a pdf document
      url: http://localhost:8081/v1/files/upload/userId
    - Add a userId at the end. This is the identification for vector db.
3. Open browser and go to url http://localhost:8081/web/index.html
    - Enter same userId and start chat

Note: Websocket will be inactive after long inactivity.
