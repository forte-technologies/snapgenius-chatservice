package dev.forte.mygeniuschat.ai.controller;
import dev.forte.mygeniuschat.ai.service.ReactiveChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ReactiveChatService chatService;

    public ChatController(ReactiveChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/stream")
    public Flux<String> streamChat(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        UUID userId = (UUID) authentication.getPrincipal();
        String message = request.get("message");
        return chatService.chatStream(userId, message);
    }
}