package dev.forte.mygeniuschat.ai.util;

public class PromptTuner {


    public static String buildDynamicSystemPrompt(PromptSettings settings) {
        return buildDynamicSystemPrompt(
                settings.getTone(),
                settings.getComplexity(),
                settings.getFocus(),
                settings.getDepth(),
                settings.getClarity()
        );
    }

    private static String buildDynamicSystemPrompt(double tone, double complexity, double focus, double depth, double clarity) {
        StringBuilder prompt = new StringBuilder();

        // Base instructions
        prompt.append("You are an AI assistant helping a student with their assignment. ");

        // TONE (7 segments from casual to formal)
        prompt.append("Use a tone that is ");
        if (tone < 0.143) {
            prompt.append("extremely casual and conversational, like chatting with a close friend. ");
        } else if (tone < 0.286) {
            prompt.append("very casual and relaxed, using everyday language and occasional slang. ");
        } else if (tone < 0.429) {
            prompt.append("casual but educational, like a friendly peer tutor. ");
        } else if (tone < 0.571) {
            prompt.append("balanced and neutral, neither too casual nor too formal. ");
        } else if (tone < 0.714) {
            prompt.append("moderately formal and professional, like a classroom instructor. ");
        } else if (tone < 0.857) {
            prompt.append("formal and scholarly, like a university professor. ");
        } else {
            prompt.append("highly formal and academic, using scholarly language appropriate for academic publications. ");
        }

        // COMPLEXITY (7 segments from simple to advanced)
        prompt.append("Present concepts with ");
        if (complexity < 0.143) {
            prompt.append("the simplest possible explanations, using only basic vocabulary and elementary concepts. ");
        } else if (complexity < 0.286) {
            prompt.append("straightforward explanations using simple terms and basic examples from everyday life. ");
        } else if (complexity < 0.429) {
            prompt.append("accessible explanations that introduce a few field-specific terms with clear definitions. ");
        } else if (complexity < 0.571) {
            prompt.append("moderate complexity, balancing accessibility with proper terminology and conceptual depth. ");
        } else if (complexity < 0.714) {
            prompt.append("somewhat advanced explanations that use appropriate field-specific vocabulary and concepts. ");
        } else if (complexity < 0.857) {
            prompt.append("advanced analysis that assumes some background knowledge and employs specialized terminology. ");
        } else {
            prompt.append("highly sophisticated analysis using specialized vocabulary, theoretical frameworks, and complex conceptual models. ");
        }

        // FOCUS (7 segments from narrow to broad)
        prompt.append("Your response should ");
        if (focus < 0.143) {
            prompt.append("address only the specific question asked with no additional information whatsoever. ");
        } else if (focus < 0.286) {
            prompt.append("stay tightly focused on the exact question with minimal supplementary details. ");
        } else if (focus < 0.429) {
            prompt.append("primarily address the specific question with only necessary contextual information. ");
        } else if (focus < 0.571) {
            prompt.append("maintain a balance between directly answering the question and providing relevant context. ");
        } else if (focus < 0.714) {
            prompt.append("address the question while exploring closely related concepts and implications. ");
        } else if (focus < 0.857) {
            prompt.append("provide a wide-ranging exploration of the topic, including various perspectives and related issues. ");
        } else {
            prompt.append("offer a comprehensive overview of the entire subject area, covering related theories, history, applications, and debates. ");
        }

        // DEPTH (7 segments from brief to thorough)
        prompt.append("Provide ");
        if (depth < 0.143) {
            prompt.append("extremely concise answers with just the essential information in the briefest form possible. ");
        } else if (depth < 0.286) {
            prompt.append("brief explanations that cover only main points without elaboration. ");
        } else if (depth < 0.429) {
            prompt.append("concise but informative explanations with limited detail. ");
        } else if (depth < 0.571) {
            prompt.append("moderately detailed explanations with a balance between brevity and thoroughness. ");
        } else if (depth < 0.714) {
            prompt.append("detailed explanations with supporting evidence and illustrative examples. ");
        } else if (depth < 0.857) {
            prompt.append("in-depth analysis with extensive supporting details, examples, and nuanced considerations. ");
        } else {
            prompt.append("exhaustively thorough explanations covering all aspects of the topic with comprehensive details, multiple examples, and full elaboration. ");
        }

        // CLARITY (7 segments from technical to clear)
        prompt.append("Make your explanations ");
        if (clarity < 0.143) {
            prompt.append("technically precise with specialized terminology, assuming substantial domain expertise. ");
        } else if (clarity < 0.286) {
            prompt.append("technically detailed, using appropriate jargon with minimal explanation of terms. ");
        } else if (clarity < 0.429) {
            prompt.append("technically oriented but with brief explanations for specialized terms. ");
        } else if (clarity < 0.571) {
            prompt.append("balanced between technical accuracy and general accessibility. ");
        } else if (clarity < 0.714) {
            prompt.append("clear and accessible, with technical terms fully explained in straightforward language. ");
        } else if (clarity < 0.857) {
            prompt.append("very clear with simplified explanations and analogies to aid understanding. ");
        } else {
            prompt.append("extremely clear and intuitive, using plain language, metaphors, and concrete examples to ensure understanding regardless of background. ");
        }

        return prompt.toString();
    }

}
