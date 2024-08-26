package com.tobiasmaneschijn.mcjsmod.javascript.interfaces;

import org.graalvm.polyglot.Context;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IJavascriptEngine {
    void initialize();
    void loadScript(String scriptPath);

    CompletableFuture<Void> evaluate(String code);

    CompletableFuture<Void> executeScript();
    void shutdown();

    void subscribeToOutput(Consumer<String> outputConsumer);
    void subscribeToError(Consumer<String> errorConsumer);
    void provideInput(String input);

    boolean isRunning();

    // New method to bind Java functions to JavaScript
    void bindFunction(String name, Function<Object[], Object> function);

    Context getContext();
}