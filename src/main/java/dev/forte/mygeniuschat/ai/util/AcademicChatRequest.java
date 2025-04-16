package dev.forte.mygeniuschat.ai.util;

public class AcademicChatRequest {
    private String message;
    private PromptSettings promptSettings;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PromptSettings getPromptSettings() {
        return promptSettings;
    }

    public void setPromptSettings(PromptSettings promptSettings) {
        this.promptSettings = promptSettings;
    }
}