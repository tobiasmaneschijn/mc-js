package com.tobiasmaneschijn.mcjsmod.network;

import com.oracle.truffle.api.strings.TruffleString;
import com.tobiasmaneschijn.mcjsmod.javascript.JSEngine;
import com.tobiasmaneschijn.mcjsmod.javascript.JSEngineManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

public class ServerUtils {
    private static JSEngineManager jsEngineManager;

    public static void setJSEngineManager(JSEngineManager manager) {
        jsEngineManager = manager;
    }

    public static JSEngineManager getJSEngineManager() {
        if (!isLogicalServer()) {
            throw new IllegalStateException("Attempted to access JS engine on logical client side");
        }
        return jsEngineManager;
    }

    public static JSEngine getJSEngine() {
        return getJSEngineManager().getEngine();
    }

    public static boolean isLogicalServer() {
        return Minecraft.getInstance().isLocalServer();
    }

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static JSEvaluationResult evaluateJS(String script) {
        if (!isLogicalServer()) {
            throw new IllegalStateException("Attempted to evaluate JS on logical client side");
        }
        JSEngine engine = getJSEngine();
        if (engine != null) {
            try {
                Value result = engine.evaluate(script);
                return new JSEvaluationResult(result, engine.getConsoleOutput());
            } catch (PolyglotException e) {
                return new JSEvaluationResult(null, engine.getConsoleOutput(), e.getMessage());
            }
        }
        return new JSEvaluationResult(null, "", "JS engine not available");
    }

    public static class JSEvaluationResult {
        public final Value result;
        public final String consoleOutput;
        public final String errorMessage;

        public JSEvaluationResult(Value result, String consoleOutput) {
            this(result, consoleOutput, null);
        }

        public JSEvaluationResult(Value result, String consoleOutput, String errorMessage) {
            this.result = result;
            this.consoleOutput = consoleOutput;
            this.errorMessage = errorMessage;
        }

        public boolean hasError() {
            return errorMessage != null;
        }
    }
    // Method to convert Value to a more usable Java object
    public static Object convertJSValue(Value value) {
        if (value == null || value.isNull()) {
            return null;
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNumber()) {
            return value.asDouble();
        } else if (value.isString()) {
            return value.asString();
        } else if (value.hasArrayElements()) {
            Object[] array = new Object[(int) value.getArraySize()];
            for (int i = 0; i < array.length; i++) {
                array[i] = convertJSValue(value.getArrayElement(i));
            }
            return array;
        } else if (value.hasMembers()) {
            Map<String, Object> map = new HashMap<>();
            for (String key : value.getMemberKeys()) {
                map.put(key, convertJSValue(value.getMember(key)));
            }
            return map;
        }
        return value.toString();
    }
}

