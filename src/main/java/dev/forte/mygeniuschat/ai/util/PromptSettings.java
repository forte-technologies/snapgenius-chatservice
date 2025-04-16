package dev.forte.mygeniuschat.ai.util;

public class PromptSettings {
    private double tone;
    private double complexity;
    private double focus;
    private double depth;
    private double clarity;

    // Getters/setters (or use Lombok/record)


    public double getTone() {
        return tone;
    }

    public void setTone(double tone) {
        this.tone = tone;
    }

    public double getComplexity() {
        return complexity;
    }

    public void setComplexity(double complexity) {
        this.complexity = complexity;
    }

    public double getFocus() {
        return focus;
    }

    public void setFocus(double focus) {
        this.focus = focus;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getClarity() {
        return clarity;
    }

    public void setClarity(double clarity) {
        this.clarity = clarity;
    }
}