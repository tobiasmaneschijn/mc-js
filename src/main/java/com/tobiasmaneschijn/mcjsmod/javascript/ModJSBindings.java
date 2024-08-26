package com.tobiasmaneschijn.mcjsmod.javascript;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IFileSystem;
import com.tobiasmaneschijn.mcjsmod.javascript.filesystem.FileSystemException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class ModJSBindings {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static boolean LOGGING_ENABLED = true;

    private static void log(String message) {
        if (LOGGING_ENABLED) {
            MCJSMod.LOGGER.info("[ModJSBindings] " + message);
        }
    }

    public static void bindFunctions(IJavascriptEngine engine, ComputerBlockEntity blockEntity) {
        log("Binding functions started");
        IFileSystem fs = blockEntity.getFileSystem();
        Context context = engine.getContext();

        // Create the 'fs' object
        Value bindings = context.getBindings("js");
        Value fsObject = context.eval("js", "({})");
        bindings.putMember("fs", fsObject);
        log("File system object created");

        // File system operations
        fsObject.putMember("createFile", (ProxyExecutable) args -> {
            try {
                String path = args[0].asString();
                String content = args[1].asString();
                log("Creating file: " + path + " (content length: " + content.length() + ")");
                fs.createFile(path, content);
                log("File created successfully: " + path);
                return null;
            } catch (FileSystemException e) {
                log("Error creating file: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        fsObject.putMember("createDirectory", (ProxyExecutable) args -> {
            try {
                String path = args[0].asString();
                log("Creating directory: " + path);
                fs.createDirectory(path);
                log("Directory created successfully: " + path);
                return null;
            } catch (FileSystemException e) {
                log("Error creating directory: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });



        fsObject.putMember("readFile", (ProxyExecutable) args -> {
            try {
                String path = args[0].asString();
                log("Reading file: " + path);
                String content = fs.readFile(path);
                log("File read successfully: " + path + " (content length: " + content.length() + ")");
                return content;
            } catch (FileSystemException e) {
                log("Error reading file: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        fsObject.putMember("writeFile", (ProxyExecutable) args -> {
            try {
                String path = args[0].asString();
                String content = args[1].asString();
                log("Writing file: " + path + " (content length: " + content.length() + ")");
                fs.writeFile(path, content);
                log("File written successfully: " + path);
                return null;
            } catch (FileSystemException e) {
                log("Error writing file: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        fsObject.putMember("deleteFile", (ProxyExecutable) args -> {
            try {
                String path = args[0].asString();
                log("Deleting file: " + path);
                fs.deleteFile(path);
                log("File deleted successfully: " + path);
                return null;
            } catch (FileSystemException e) {
                log("Error deleting file: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        fsObject.putMember("deleteDirectory", (ProxyExecutable) args -> {
            try {
                String path = args[0].asString();
                log("Deleting directory: " + path);
                fs.deleteDirectory(path);
                log("Directory deleted successfully: " + path);
                return null;
            } catch (FileSystemException e) {
                log("Error deleting directory: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        fsObject.putMember("listFiles", (ProxyExecutable) args -> {
            try {
                String path = args[0].asString();
                log("Listing files in directory: " + path);
                var files = fs.listFiles(path);
                log("Files listed successfully for: " + path + " (count: " + files.size() + ")");
                return files;
            } catch (FileSystemException e) {
                log("Error listing files: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        fsObject.putMember("exists", (ProxyExecutable) args -> {
            try {
                String path = args[0].asString();
                log("Checking if exists: " + path);
                boolean exists = fs.exists(path);
                log("Existence check result for " + path + ": " + exists);
                return exists;
            } catch (Exception e) {
                log("Error checking file existence: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        fsObject.putMember("isDirectory", (ProxyExecutable) args -> {
            String path = args[0].asString();
            log("Checking if is directory: " + path);
            boolean isDir = fs.isDirectory(path);
            log("Is directory check result for " + path + ": " + isDir);
            return isDir;
        });

        bindings.putMember("system", context.eval("js", "({})"));
        Value systemObject = bindings.getMember("system");
        log("System object created");

        systemObject.putMember("clearInput", (ProxyExecutable) args -> {
            blockEntity.signalClearInput();
            return null;
        });

        // Clear terminal by adding a bunch of newlines
        systemObject.putMember("clearTerminal", (ProxyExecutable) args -> {
            blockEntity.handleOutput("\n".repeat(50));
            return null;
        });

        systemObject.putMember("readLine", (ProxyExecutable) args -> {
            String prompt = args[0].asString();
            log("ReadLine called with prompt: " + prompt);
            if(prompt.trim().isEmpty()) {
                log("Empty prompt, returning null");
                return null;
            }
            log("Creating Promise for readLine");

            return context.eval("js",
                    "(function(prompt) { " +
                            "  return new Promise((resolve, reject) => { " +
                            "    readLineInternal(prompt, resolve, reject); " +
                            "  }); " +
                            "})").execute(prompt);
        });

        context.getBindings("js").putMember("readLineInternal", (ProxyExecutable) args -> {
            String prompt = args[0].asString();
            Value resolve = args[1].as(Value.class);
            Value reject = args[2].as(Value.class);

            log("readLineInternal called with prompt: " + prompt);
            try {
                CompletableFuture<String> futureInput = blockEntity.requestInput(prompt);
                futureInput.thenAccept(resolve::execute).exceptionally(ex -> {
                    reject.execute(ex.getMessage());
                    return null;
                });
            } catch (Exception e) {
                log("Error in readLineInternal: " + e.getMessage());
                reject.execute(e.getMessage());
            }
            return null;
        });

        // Process operations
        bindings.putMember("process", context.eval("js", "({})")); // Create process object
        Value processObject = bindings.getMember("process");
        log("Process object created");

        processObject.putMember("start", (ProxyExecutable) args -> {
            String scriptPath = args[0].asString();
            try {
                log("Starting process: " + scriptPath);
                String script = fs.readFile(scriptPath);
                log("Script loaded, length: " + script.length());
                engine.evaluate(script);
                log("Process started successfully: " + scriptPath);
                return null;
            } catch (FileSystemException e) {
                log("Failed to start process: " + e.getMessage());
                throw new RuntimeException("Failed to start process: " + e.getMessage());
            }
        });

        // Console operations
        bindings.putMember("console", context.eval("js", "({})")); // Create console object
        Value consoleObject = bindings.getMember("console");
        log("Console object created");

        consoleObject.putMember("log", (ProxyExecutable) args -> {
            String message = args[0].toString();
            log("Console log: " + message);
            blockEntity.handleOutput(message);
            return null;
        });

        consoleObject.putMember("error", (ProxyExecutable) args -> {
            String message = args[0].toString();
            log("Console error: " + message);
            blockEntity.handleOutput("<ERROR> " + message + " </ERROR>");
            return null;
        });


        bindings.putMember("setTimeout", (ProxyExecutable) args -> {
            Value callback = args[0];
            int delay = args[1].asInt();

            scheduler.schedule(() -> {
                try {
                    callback.execute();
                } catch (Exception e) {
                    log("Error in setTimeout callback: " + e.getMessage());
                }
            }, delay, TimeUnit.MILLISECONDS);

            return null;
        });



        log("Binding functions completed");
    }

    // Method to enable/disable logging
    public static void setLoggingEnabled(boolean enabled) {
        LOGGING_ENABLED = enabled;
        log("Logging " + (enabled ? "enabled" : "disabled"));
    }

    public static void shutdown() {
        scheduler.shutdown();
    }
}