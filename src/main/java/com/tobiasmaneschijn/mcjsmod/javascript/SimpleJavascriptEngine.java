package com.tobiasmaneschijn.mcjsmod.javascript;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;

import javax.script.*;
import java.io.Reader;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleJavascriptEngine {
    private ScriptEngine engine;
    private StringWriter consoleOutput;
    private AtomicBoolean isBusy = new AtomicBoolean(false);

    public void init() {
        consoleOutput = new StringWriter();
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");

        if (engine == null) {
            MCJSMod.LOGGER.warn("JavaScript engine not found. Using fallback interpreter.");
            engine = new FallbackJavaScriptEngine();
        }

        setupConsoleLog();
    }

    private void setupConsoleLog() {
        try {
            engine.put("console", new SimpleConsole(consoleOutput));
        } catch (Exception e) {
            MCJSMod.LOGGER.error("Failed to set up console logging", e);
        }
    }

    public Object execute(String script) throws ScriptException {
        if (isBusy.getAndSet(true)) {
            throw new IllegalStateException("Engine is busy");
        }
        try {
            consoleOutput.getBuffer().setLength(0); // Clear previous output
            return engine.eval(script);
        } finally {
            isBusy.set(false);
        }
    }

    public String getConsoleOutput() {
        return consoleOutput.toString();
    }

    // Implement other necessary methods...

    private static class SimpleConsole {
        private final StringWriter writer;

        SimpleConsole(StringWriter writer) {
            this.writer = writer;
        }

        public void log(Object... args) {
            for (Object arg : args) {
                writer.write(String.valueOf(arg));
                writer.write(" ");
            }
            writer.write("\n");
        }
    }

    private static class FallbackJavaScriptEngine implements ScriptEngine {
        @Override
        public Object eval(String script, ScriptContext context) throws ScriptException {
            return null;
        }

        @Override
        public Object eval(Reader reader, ScriptContext context) throws ScriptException {
            return null;
        }

        @Override
        public Object eval(String script) throws ScriptException {
            return null;
        }

        @Override
        public Object eval(Reader reader) throws ScriptException {
            return null;
        }

        @Override
        public Object eval(String script, Bindings n) throws ScriptException {
            return null;
        }

        @Override
        public Object eval(Reader reader, Bindings n) throws ScriptException {
            return null;
        }

        @Override
        public void put(String key, Object value) {

        }

        @Override
        public Object get(String key) {
            return null;
        }

        @Override
        public Bindings getBindings(int scope) {
            return null;
        }

        @Override
        public void setBindings(Bindings bindings, int scope) {

        }

        @Override
        public Bindings createBindings() {
            return null;
        }

        @Override
        public ScriptContext getContext() {
            return null;
        }

        @Override
        public void setContext(ScriptContext context) {

        }

        @Override
        public ScriptEngineFactory getFactory() {
            return null;
        }
        // Implement a simple interpreter here...
        // This is a placeholder and should be replaced with a basic implementation
        // that can handle simple expressions and function calls
    }
}