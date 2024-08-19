package com.tobiasmaneschijn.mcjsmod.javascript.interfaces;

import java.util.function.Function;

public interface IJavascriptEngine {

    void init();

    Object execute(String script);

    void interrupt();

    String getTerminalContents();

    void bindFunction(String name, Function<Object[], Object> function);

    void close();

    // New methods for state management
    String getState();
    void setState(String state);

    // New method to clear the terminal contents
    void clearTerminal();

    // New method to get the last execution result
    Object getLastResult();

    // New method to check if the engine is busy
    boolean isBusy();

    // New method to set execution timeout
    void setTimeout(long milliseconds);
}