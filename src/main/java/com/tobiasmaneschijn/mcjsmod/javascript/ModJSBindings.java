package com.tobiasmaneschijn.mcjsmod.javascript;

import com.tobiasmaneschijn.mcjsmod.network.ServerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ModJSBindings {

    public static void bindFunctions() {

        JSEngine engine = ServerUtils.getJSEngine();

        if (engine != null) {
            engine.bindMinecraftFunction("displayChatMessage", (args) -> {
                if (args.length == 1) {
                    displayChatMessage(args[0].asString());
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
