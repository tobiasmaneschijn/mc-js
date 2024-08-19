package com.tobiasmaneschijn.mcjsmod.javascript;

import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.*;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
public class JSEngine {
    private Context context;
    private ByteArrayOutputStream consoleOutput;

    public JSEngine() {
        consoleOutput = new ByteArrayOutputStream();
        context = Context.newBuilder("js")
                .allowAllAccess(false)
                .allowNativeAccess(false)
                .allowCreateThread(false)
                .allowHostAccess(HostAccess.NONE)
                .out(consoleOutput)
                .err(consoleOutput)
                .build();

        context.getBindings("js").putMember("console", new ProxyObject() {
            @Override
            public Object getMember(String key) {
                if (key.equals("log")) {
                    return new ProxyExecutable() {
                        public Object execute(Value... args) {
                            for (Value arg : args) {
                                try {
                                    consoleOutput.write(convertValueToString(arg).getBytes(StandardCharsets.UTF_8));
                                    consoleOutput.write(" ".getBytes(StandardCharsets.UTF_8));
                                } catch (Exception e) {
                                    // Handle or log the error
                                }
                            }
                            try {
                                consoleOutput.write("\n".getBytes(StandardCharsets.UTF_8));
                            } catch (Exception e) {
                                // Handle or log the error
                            }
                            return Value.asValue(null);
                        }
                    };
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
        });
    }

    public Value evaluate(String script) {
        consoleOutput.reset(); // Clear previous output
        return context.eval("js", script);
    }

    public String getConsoleOutput() {
        return  convertValueToString(Value.asValue(consoleOutput.toString()));
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


    public void bindMinecraftFunction(String name, ProxyExecutable function) {
        context.getBindings("js").putMember(name, function);
    }

    public void close() {
        context.close();
    }
}
