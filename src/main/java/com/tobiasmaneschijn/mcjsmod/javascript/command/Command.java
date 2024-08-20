package com.tobiasmaneschijn.mcjsmod.javascript.command;


public class Command {
    private final String name;
    private final String description;
    private final String scriptContent;

    public Command(String name, String description, String scriptContent) {
        this.name = name;
        this.description = description;
        this.scriptContent = scriptContent;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getScriptContent() {
        return scriptContent;
    }
}