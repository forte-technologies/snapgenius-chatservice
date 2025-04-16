package dev.forte.mygeniuschat.ai.controller;
import dev.forte.mygeniuschat.ai.service.AcademicGeneralChatService;
import dev.forte.mygeniuschat.ai.service.GeneralChatService;
import dev.forte.mygeniuschat.ai.service.StrictRagChat;
import dev.forte.mygeniuschat.ai.util.AcademicChatRequest;
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

    private final StrictRagChat strictRagChatService;
    private final GeneralChatService generalChatService;
    private final AcademicGeneralChatService academicGeneralChatService;

    public ChatController(StrictRagChat strictRagChatService, GeneralChatService generalChatService, AcademicGeneralChatService academicGeneralChatService) {
        this.strictRagChatService = strictRagChatService;
        this.generalChatService = generalChatService;
        this.academicGeneralChatService = academicGeneralChatService;
    }

    @PostMapping(value = "/stream/rag")
    public Flux<String> streamStrictRagChat(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        UUID userId = (UUID) authentication.getPrincipal();
        String message = request.get("message");
        return strictRagChatService.chatStream(userId, message);
    }

    @PostMapping(value = "/stream/general")
    public Flux<String> streamGeneralChat(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        UUID userId = (UUID) authentication.getPrincipal();
        String message = request.get("message");
        return generalChatService.generalChatStream(userId, message);
    }

    @PostMapping(value = "/stream/academic")
    public Flux<String> streamGeneralAcademicChat(
            Authentication authentication,
            @RequestBody AcademicChatRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();
        return academicGeneralChatService.generalChatStream(
                userId,
                request.getMessage(),
                request.getPromptSettings()
        );
    }
}