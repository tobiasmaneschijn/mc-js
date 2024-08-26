package com.tobiasmaneschijn.mcjsmod.javascript;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GraalJavascriptEngine implements IJavascriptEngine {
    private Context context;
    private String scriptContent;
    private final BlockingQueue<String> inputQueue;
    private Consumer<String> outputConsumer;
    private Consumer<String> errorConsumer;
    private boolean isRunning;
    private Value processInputFunction;
    private final ExecutorService executorService;

    public GraalJavascriptEngine() {
        this.inputQueue = new LinkedBlockingQueue<>();
        this.isRunning = false;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void initialize() {
        try {
            Context.Builder contextBuilder = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .allowCreateThread(true)
                    .allowIO(IOAccess.NONE)
                    .allowCreateProcess(false)
                    .allowExperimentalOptions(true)
                    .option("js.ecmascript-version", "2022")
                    .option("js.nashorn-compat", "true");


            context = contextBuilder.build();


            MCJSMod.LOGGER.info("GraalJavascriptEngine initialized successfully");
        } catch (IllegalArgumentException e) {
            MCJSMod.LOGGER.error("Error initializing GraalJavascriptEngine: " + e.getMessage());
            throw new RuntimeException("Failed to initialize GraalJavascriptEngine", e);
        }

        // Bind Java methods to JavaScript global objects
        context.getBindings("js").putMember("javaRead", new ProxyExecutable() {
            @Override
            public Object execute(Value... arguments) {
                try {
                    return inputQueue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        });

        context.getBindings("js").putMember("javaWrite", new ProxyExecutable() {
            @Override
            public Object execute(Value... arguments) {
                if (arguments.length > 0 && outputConsumer != null) {
                    outputConsumer.accept(arguments[0].asString());
                }
                return null;
            }
        });

        context.getBindings("js").putMember("javaWriteErr", new ProxyExecutable() {
            @Override
            public Object execute(Value... arguments) {
                if (arguments.length > 0 && errorConsumer != null) {
                    errorConsumer.accept(arguments[0].asString());
                }
                return null;
            }
        });

        context.eval("js", "function processInput(input) { console.log('Processing input: ' + input); }");
        processInputFunction = context.getBindings("js").getMember("processInput");

        if (processInputFunction == null || !processInputFunction.canExecute()) {
            throw new IllegalStateException("Failed to initialize processInput function");
        }
    }

    @Override
    public void loadScript(String code) {
        scriptContent = code;
    }

    @Override
    public CompletableFuture<Void> evaluate(String code) {
        return CompletableFuture.runAsync(() -> {
            isRunning = true;
            try {
                MCJSMod.LOGGER.info("Evaluating code: " + code.substring(0, Math.min(code.length(), 100)) + "...");
                Value result = context.eval("js", code);
                if (result.canExecute()) {
                    MCJSMod.LOGGER.info("Executing result of evaluation");
                    result.execute();
                }
                MCJSMod.LOGGER.info("Code evaluation completed successfully");
            } catch (PolyglotException e) {
                MCJSMod.LOGGER.error("Error executing JavaScript: " + e.getMessage(), e);
                errorConsumer.accept("Error executing JavaScript: " + e.getMessage());
            } finally {
                isRunning = false;
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> executeScript() {
        if (scriptContent == null) {
            MCJSMod.LOGGER.error("No script loaded. Call loadScript() first.");
            throw new IllegalStateException("No script loaded. Call loadScript() first.");
        }
        return CompletableFuture.runAsync(() -> {
            isRunning = true;
            try {
                MCJSMod.LOGGER.info("Executing script: " + scriptContent.substring(0, Math.min(scriptContent.length(), 100)) + "...");
                context.eval("js", scriptContent);
                MCJSMod.LOGGER.info("Script execution completed successfully");
            } catch (PolyglotException e) {
                MCJSMod.LOGGER.error("Error executing JavaScript script: " + e.getMessage(), e);
                errorConsumer.accept("Error executing JavaScript: " + e.getMessage());
            } finally {
                isRunning = false;
            }
        }, executorService);
    }


    @Override
    public void shutdown() {
        isRunning = false;
        if (context != null) {
            context.close();
        }
        executorService.shutdown();
    }

    @Override
    public void subscribeToOutput(Consumer<String> outputConsumer) {
        this.outputConsumer = outputConsumer;
    }

    @Override
    public void subscribeToError(Consumer<String> errorConsumer) {
        this.errorConsumer = errorConsumer;
    }

    @Override
    public void provideInput(String input) {
        CompletableFuture.runAsync(() -> {
            try {
                if (isRunning && processInputFunction != null) {
                    MCJSMod.LOGGER.info("Providing input: " + input);
                    processInputFunction.execute(input);
                } else {
                    inputQueue.offer(input);
                    MCJSMod.LOGGER.info("Queued input: " + input);
                    processQueuedInput(); // Try to process queued input
                }
            } catch (Exception e) {
                if (errorConsumer != null) {
                    errorConsumer.accept("Error processing input: " + e.getMessage());
                }
            }
        }, executorService).exceptionally(throwable -> {
            if (errorConsumer != null) {
                errorConsumer.accept("Async execution error: " + throwable.getMessage());
            }
            return null;
        });
    }

    private void processQueuedInput() {
        if (!isRunning || processInputFunction == null) {
            return;
        }
        String input;
        while ((input = inputQueue.poll()) != null) {
            final String currentInput = input;
            try {
                MCJSMod.LOGGER.info("Processing queued input: " + currentInput);
                processInputFunction.execute(currentInput);
            } catch (Exception e) {
                if (errorConsumer != null) {
                    errorConsumer.accept("Error processing queued input: " + e.getMessage());
                }
            }
        }
    }
    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void bindFunction(String name, Function<Object[], Object> function) {
        context.getBindings("js").putMember(name, new ProxyExecutable() {
            @Override
            public Object execute(Value... arguments) {
                Object[] args = new Object[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    args[i] = arguments[i].as(Object.class);
                }
                return function.apply(args);
            }
        });
    }

    @Override
    public Context getContext() {
        return context;
    }
}