package dev.forte.mygeniuschat.ai.service;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.context.ThreadLocalAccessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;

import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
import static org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor.FILTER_EXPRESSION;

@Service
public class StrictRagChat {

    private final ChatClient chatClient;

    private final Map<UUID, LocalDateTime> lastAccessTimes = new ConcurrentHashMap<>();

    public StrictRagChat(
            @Qualifier("ragChat") ChatClient.Builder builder, VectorStore vectorStore,
            @Qualifier("defaultChatMemory") ChatMemory chatMemory,
            ThreadLocalAccessor<SecurityContext> securityContextAccessor) {


        ContextRegistry contextRegistry = ContextRegistry.getInstance();
        contextRegistry.registerThreadLocalAccessor(securityContextAccessor);

        var qaAdvisor = new QuestionAnswerAdvisor(vectorStore,
                SearchRequest.builder().similarityThreshold(0.4).topK(25).build());

        VectorStoreChatMemoryAdvisor vectorStoreChatMemoryAdvisor =
                VectorStoreChatMemoryAdvisor.builder(vectorStore).build();


        this.chatClient = builder
                .defaultSystem("You are a helpful AI assistant for all things academia...")
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        vectorStoreChatMemoryAdvisor,
                        qaAdvisor)
                .build();
    }

    public Flux<String> chatStream(UUID userId, String userMessageContent) {
        lastAccessTimes.put(userId, LocalDateTime.now());

        var snapshot = ContextSnapshotFactory.builder().build().captureAll();
        // Create a consistent conversationId using the userId
        String conversationId = userId.toString();
        String filterExpression = "user_id == '" + userId.toString() + "'";

        return chatClient.prompt()

                        .user(userMessageContent)
                        .advisors(a -> a
                                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20)
                                .param(FILTER_EXPRESSION, filterExpression))
                        .stream().content().contextWrite(snapshot::updateContext);

    }
}