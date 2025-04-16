package dev.forte.mygeniuschat.ai.service;

import dev.forte.mygeniuschat.ai.util.PromptSettings;
import dev.forte.mygeniuschat.ai.util.PromptTuner;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.context.ThreadLocalAccessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor.FILTER_EXPRESSION;


@Service
public class AcademicGeneralChatService {


    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Map<UUID, LocalDateTime> lastAccessTimes = new ConcurrentHashMap<>();

    public AcademicGeneralChatService(@Qualifier("academicChat")OpenAiChatModel openAiChatModel,
                                      @Qualifier("defaultChatMemory") ChatMemory chatMemory, VectorStore vectorStore,
                                      ThreadLocalAccessor<SecurityContext> securityContextAccessor) {

        ContextRegistry contextRegistry = ContextRegistry.getInstance();
        contextRegistry.registerThreadLocalAccessor(securityContextAccessor);
        this.vectorStore = vectorStore;
        this.chatClient = ChatClient.builder(openAiChatModel).build();
    }

    public Flux<String> generalChatStream(UUID userId, String userMessageContent, PromptSettings promptSettings) {
        lastAccessTimes.put(userId, LocalDateTime.now());

        var snapshot = ContextSnapshotFactory.builder().build().captureAll();

        String settings = PromptTuner.buildDynamicSystemPrompt(promptSettings);

        String conversationId = userId.toString();
        String filterExpression = "user_id == '" + userId.toString() + "'";

        return Mono.fromCallable(() -> {
                    // Execute vector search on an appropriate scheduler
                    return this.vectorStore.similaritySearch(SearchRequest.builder()
                            .query(userMessageContent) // Also fixed to use actual user message
                            .topK(25)
                            .similarityThreshold(.3)
                            .filterExpression(filterExpression)
                            .build());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(results -> {
                    // Process results to create enhanced message
                    StringBuilder context = new StringBuilder();
                    if (!results.isEmpty()) {
                        context.append("Here is some relevant information from the user's documents or images:\n\n");
                        for (Document document : results) {
                            context.append("---\n");
                            context.append(document.getText()).append("\n");
                            context.append("---\n\n");
                        }
                    }

                    String enhancedSystemMessage = "You are a helpful AI assistant for all things academia. ";
                    if (!context.isEmpty()) {
                        enhancedSystemMessage += "Below is some context that will relevant to the user's question. " +
                                "Prioritize using this information, but also rely on your general knowledge " +
                                "to provide the most helpful response.\n\n" + context;
                    }

                    return enhancedSystemMessage;
                })
                .flatMapMany(enhancedSystemMessage ->
                        this.chatClient.prompt()
                                .system(settings + " " + enhancedSystemMessage)
                                .user(userMessageContent)
                                .advisors(a -> a
                                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20)
                                        .param(FILTER_EXPRESSION, filterExpression))
                                .stream().content()
                )
                .contextWrite(snapshot::updateContext);
    }
}