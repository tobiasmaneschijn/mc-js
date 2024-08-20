package com.tobiasmaneschijn.mcjsmod.javascript;

import com.google.gson.Gson;
import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.javascript.command.Command;
import com.tobiasmaneschijn.mcjsmod.javascript.command.CommandLoader;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

public class ModJSBindings {

    private static final Gson GSON = new Gson();

    public static void bindFunctions(IJavascriptEngine engine, ComputerBlockEntity blockEntity) {

        if (engine != null) {
            engine.bindFunction("displayChatMessage", (args) -> {
                if (args.length == 1) {
                    displayChatMessage(args[0].toString());
                }
                return "Sent message in chat";
            });
            engine.bindFunction("getCommands", (args) -> {
                return convertCommandsToJson(commands());
            });
            engine.bindFunction("clearTerminal", (args) -> {
                ClearTerminal(blockEntity);
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

    public static Map<String, Command> commands() {
        return CommandLoader.getCommands();
    }

    // Converts the commands map to a JSON string
    public static String convertCommandsToJson(Map<String, Command> commands) {
        Map<String, Map<String, String>> result = new HashMap<>();
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            Map<String, String> commandInfo = new HashMap<>();
            commandInfo.put("name", entry.getValue().getName());
            commandInfo.put("description", entry.getValue().getDescription());
            result.put(entry.getKey(), commandInfo);
        }

        // Convert the map to a JSON string
        String jsonString = GSON.toJson(result);

        // Log for debugging
        MCJSMod.LOGGER.info("Converted Commands to JSON: " + jsonString);
        return jsonString;
    }


    public static void ClearTerminal(ComputerBlockEntity blockEntity) {
        blockEntity.clearTerminal();
    }

}
