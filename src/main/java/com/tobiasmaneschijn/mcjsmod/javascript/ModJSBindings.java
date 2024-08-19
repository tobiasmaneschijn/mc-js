package com.tobiasmaneschijn.mcjsmod.javascript;

import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import com.tobiasmaneschijn.mcjsmod.network.ServerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ModJSBindings {

    public static void bindFunctions() {

        IJavascriptEngine engine = ServerUtils.getJSEngine();
        if (engine != null) {
            engine.bindFunction("displayChatMessage", (args) -> {
                if (args.length == 1) {
                    displayChatMessage(args[0].toString());
                }
                return null;
            });
        }
    }

    public static void displayChatMessage(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(message), false);
        }
    }
}
