package com.tobiasmaneschijn.mcjsmod.javascript;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraalJavascriptEngine implements IJavascriptEngine {

    private Context context;
    private ByteArrayOutputStream consoleOutput;
    private Object lastResult;
    private AtomicBoolean isBusy = new AtomicBoolean(false);
    private long timeout = 5000; // Default timeout of 5 seconds

    public GraalJavascriptEngine() {
        init();
    }

    @Override
    public void init() {
        consoleOutput = new ByteArrayOutputStream();

        // Suppress Truffle logs
        Logger.getLogger("org.graalvm.truffle").setLevel(Level.OFF);


        // Redirect System.err to capture and suppress GraalVM warnings
        PrintStream originalErr = System.err;
        ByteArrayOutputStream warningCapture = new ByteArrayOutputStream();
        System.setErr(new PrintStream(warningCapture));

        try {
            context = Context.newBuilder("js")
                    .allowAllAccess(false)
                    .allowCreateThread(false)
                    .allowHostAccess(HostAccess.NONE)
                    .option("engine.WarnInterpreterOnly", "false") // Suppress interpreter warning
                    .out(consoleOutput)
                    .err(consoleOutput)
                    .build();
        } finally {
            // Restore original System.err
            System.setErr(originalErr);
        }

        setupConsoleLog();

        // Log captured warnings at debug level
        String warnings = warningCapture.toString(StandardCharsets.UTF_8);
        if (!warnings.isEmpty()) {
            MCJSMod.LOGGER.debug("Suppressed GraalVM warnings: " + warnings);
        }
    }

    private void setupConsoleLog() {
        context.getBindings("js").putMember("console", new ProxyObject() {
            @Override
            public Object getMember(String key) {
                if (key.equals("log")) {
                    return (ProxyExecutable) this::logToConsole;
                }
                return null;
            }

            @Override
            public Object getMemberKeys() {
                return Collections.singleton("log");
            }

            @Override
            public boolean hasMember(String key) {
                return key.equals("log");
            }

            @Override
            public void putMember(String key, Value value) {
                // Do nothing, we don't allow adding new members
            }

            private Object logToConsole(Value... args) {
                for (Value arg : args) {
                    try {
                        consoleOutput.write(convertValueToString(arg).getBytes(StandardCharsets.UTF_8));
                        consoleOutput.write(" ".getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        MCJSMod.LOGGER.error("Error writing to console output", e);
                    }
                }
                try {
                    consoleOutput.write("\n".getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    MCJSMod.LOGGER.error("Error writing newline to console output", e);
                }

                MCJSMod.LOGGER.info("GraalVM Console: " + consoleOutput.toString());

                return Value.asValue(null);
            }
        });
    }

    @Override
    public Object execute(String script) {
        if (isBusy.getAndSet(true)) {
            throw new IllegalStateException("Engine is busy");
        }
        try {
            consoleOutput.reset(); // Clear previous output
            lastResult = context.eval("js", script);
            return lastResult;
        } finally {
            isBusy.set(false);
        }
    }

    @Override
    public void interrupt() {
        try {
            context.interrupt(Duration.ofMillis(timeout));
        } catch (TimeoutException e) {
            MCJSMod.LOGGER.error("Interrupting JS execution timed out");
        }
    }

    @Override
    public String getTerminalContents() {
        return consoleOutput.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void bindFunction(String name, Function<Object[], Object> function) {
        context.getBindings("js").putMember(name, (ProxyExecutable) (Value[] args) -> {
            // Convert Value[] to Object[]
            Object[] convertedArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                convertedArgs[i] = args[i].as(Object.class);
            }
            // Apply the converted arguments to the function
            return function.apply(convertedArgs);
        });
    }
    @Override
    public void close() {
        context.close();
    }

    @Override
    public String getState() {
        // This is a simple implementation. You might want to serialize more state information.
        return context.getBindings("js").toString();
    }

    @Override
    public void setState(String state) {
        // This is a simple implementation. You might need to parse the state string and set up the context accordingly.
        context.eval("js", state);
    }

    @Override
    public void clearTerminal() {
        consoleOutput.reset();
    }

    @Override
    public Object getLastResult() {
        return lastResult;
    }

    @Override
    public boolean isBusy() {
        return isBusy.get();
    }

    @Override
    public void setTimeout(long milliseconds) {
        this.timeout = milliseconds;
    }

    private String convertValueToString(Value value) {
        if (value.isString()) {
            return value.asString();
        } else if (value.hasArrayElements()) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < value.getArraySize(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(convertValueToString(value.getArrayElement(i)));
            }
            sb.append("]");
            return sb.toString();
        } else if (value.hasMembers()) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (String key : value.getMemberKeys()) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(key).append(": ").append(convertValueToString(value.getMember(key)));
            }
            sb.append("}");
            return sb.toString();
        } else {
            return value.toString();
        }
    }

    @Override
    public Context getContext() {
        return context;
    }
}