# MyGenius Chat Microservice

This microservice is a component of the broader **snapGenius** application, responsible for handling all chat-related functionalities, with a primary focus on Retrieval-Augmented Generation (RAG). It interacts with AI models and a vector database to provide intelligent, context-aware responses based on user-uploaded content (managed by the main backend application).
https://snapgenius.app/
## Core Functionalities

*   **Retrieval-Augmented Generation (RAG):** Provides chat endpoints that leverage user-specific data stored as vector embeddings (e.g., from image analysis or document text extraction performed by the main backend). It retrieves relevant context from the vector store based on the user's query and uses it to generate more accurate and relevant responses from the AI model.
*   **General Chat:** Offers a general chat mode that uses context from the vector store but also relies on the AI model's general knowledge.
*   **Streaming Responses:** Utilizes Spring MVC's asynchronous request processing capabilities to stream responses back to the client (e.g., using Server-Sent Events). This is achieved by returning `reactor.core.publisher.Flux<String>` from controller methods, allowing for real-time updates in the chat interface without requiring the full Spring WebFlux dependency.
*   **Contextual Awareness:** Maintains chat history per user session using Spring AI's `ChatMemory` and associates vector data specifically with users via metadata filtering (`user_id`).
*   **Security:** Secures chat endpoints, requiring a valid JWT token (issued by the main backend) for access.

## Technology Stack

*   **Framework:** Spring Boot 3.x (using Spring Web MVC)
*   **AI Integration:** Spring AI
    *   **LLM:** OpenAI (GPT-4o or configurable)
    *   **Embeddings:** OpenAI (text-embedding-ada-002 or configurable)
*   **Vector Database:** PostgreSQL with PgVector extension (via `spring-ai-pgvector-store-spring-boot-starter`)
*   **Database:** PostgreSQL (for vector store)
*   **Asynchronous Handling:** Project Reactor (`Flux`, `Mono`) for internal processing, handled by Spring MVC for streaming responses.
*   **Security:** Spring Security, JSON Web Tokens (JWT)
*   **Context Propagation:** Micrometer Context Propagation (`micrometer-context-registry`, `ThreadLocalAccessor`) to ensure `SecurityContext` is available across asynchronous threads (e.g., during streaming and vector search operations).
*   **Build Tool:** Apache Maven
*   **Language:** Java 21

## Architecture & Interaction

This microservice expects to receive requests from the main snapGenius frontend or backend, specifically for chat interactions.

1.  **Authentication:** Requests to `/api/chat/**` must include a `Bearer` token (JWT) in the `Authorization` header. This token is generated by the main backend upon successful user login. The `ChatTokenAuthenticationFilter` validates the token and extracts the `userId` (UUID).
2.  **Request Handling:** The `ChatController` receives chat messages for either RAG (`/api/chat/stream/rag`) or general chat (`/api/chat/stream/general`).
3.  **Service Layer:**
    *   `StrictRagChat`: Handles RAG requests, using Spring AI Advisors (`QuestionAnswerAdvisor`, `VectorStoreChatMemoryAdvisor`) to automatically retrieve relevant documents matching the `userId` from the `PgVectorStore` and incorporate them into the prompt for the AI model.
    *   `GeneralChatService`: Handles general chat requests. It performs a similarity search against the `PgVectorStore` (filtered by `userId`) to potentially enhance the system prompt with relevant context but allows the AI to rely on its general knowledge as well.
4.  **Vector Store:** Interacts with the configured `PgVectorStore` (PostgreSQL + PgVector) to find relevant text chunks based on semantic similarity to the user's query, filtered by the authenticated `userId`.
5.  **AI Model:** Sends prompts (potentially augmented with retrieved context) to the configured OpenAI chat model.
6.  **Streaming Response:** Streams the AI model's response back to the client as a `Flux<String>`, handled by Spring MVC.
7.  **Security Context Propagation:** Uses `SecurityContextThreadLocalAccessor` registered with `ContextRegistry` to ensure the `Authentication` object (containing the `userId`) is accessible in the reactive pipeline and background threads used by services (e.g., `Schedulers.boundedElastic()` for vector search).

## API Endpoints

*   `POST /api/chat/stream/rag`: Initiates a RAG chat stream. Requires authentication.
    *   **Request Body:** `{"message": "Your chat message"}`
    *   **Response:** `text/event-stream` (SSE) of AI-generated response chunks.
*   `POST /api/chat/stream/general`: Initiates a general chat stream. Requires authentication.
    *   **Request Body:** `{"message": "Your chat message"}`
    *   **Response:** `text/event-stream` (SSE) of AI-generated response chunks.

## Configuration

Key configuration properties are managed in `src/main/resources/application.properties` (and profile-specific variants like `application-dev.properties`). Environment variables are recommended for sensitive data.

*   `server.port`: Port the microservice runs on (default: `7050`).
*   `spring.datasource.url`: JDBC URL for the PostgreSQL database.
*   `spring.datasource.username`: Database username.
*   `spring.datasource.password`: Database password.
*   `spring.ai.openai.api-key`: Your OpenAI API key.
*   `spring.ai.vectorstore.pgvector.table-name`: Name of the table used by PgVectorStore (default: `vector_store`). Ensure this table exists with the correct schema.
*   `jwt.secret`: The **same** secret key used by the main backend application to sign JWT tokens.
*   `allowed.origins`: Comma-separated list of origins allowed for CORS.
*   `spring.threads.virtual.enabled=true`: Leverages virtual threads for I/O intensive tasks.

## Running the Service

### Prerequisites

*   Java 21 SDK
*   Access to a PostgreSQL database with the PgVector extension enabled.
*   OpenAI API Key.
*   The main snapGenius backend running (to provide JWT tokens).

### Development

1.  **Configure:** Set the required properties in `src/main/resources/application-dev.properties` or via environment variables (e.g., `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `OPEN_AI_KEY`, `JWT_SECRET`, `ALLOWED_ORIGINS`).
2.  **Run:** Use the Spring Boot Maven plugin:
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
    ```

Ensure your `application-dev.properties` points to this Dockerized database (`jdbc:postgresql://localhost:5436/mygenius` by default).

### Production / Deployment

Build the application JAR:

```bash
./mvnw clean package
```

Run the JAR, providing necessary configuration via environment variables:

```bash
export DATABASE_URL=...
export DB_USERNAME=...
export DB_PASSWORD=...
export OPEN_AI_KEY=...
export JWT_SECRET=...
export ALLOWED_ORIGINS=...
java -jar target/mygenius-chat-microservice-0.0.1-SNAPSHOT.jar
```
