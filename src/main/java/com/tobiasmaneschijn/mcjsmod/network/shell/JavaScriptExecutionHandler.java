package com.tobiasmaneschijn.mcjsmod.network.shell;


import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class JavaScriptExecutionHandler {

    public static void handleJavaScriptExecutionPayload(JavaScriptExecutionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player() instanceof ServerPlayer ? (ServerPlayer) context.player() : null;
            if (player == null) {
                MCJSMod.LOGGER.error("Failed to get ServerPlayer in JavaScript execution handler");
                return;
            }

            ServerLevel level = player.serverLevel();
            BlockPos pos = payload.pos();
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof ComputerBlockEntity computerBlockEntity) {
                IJavascriptEngine engine = computerBlockEntity.getJavascriptEngine();

                if (!engine.isBusy()) {
                    engine.setTimeout(5000); // 5 second timeout
                    try {
                        Object result = engine.execute(payload.script());
                        String output = engine.getTerminalContents();

                        // Send the result back to the client
                        sendJavaScriptResult(player, pos, output, result);

                        // Save the engine state after execution
                        computerBlockEntity.saveEngineState(engine.getState());
                    } catch (Exception e) {
                        MCJSMod.LOGGER.error("Error executing JavaScript: ", e);
                        sendJavaScriptResult(player, pos, "Error: " + e.getMessage(), null);
                    }
                } else {
                    sendJavaScriptResult(player, pos, "Computer is busy. Please wait and try again.", null);
                }
            } else {
                MCJSMod.LOGGER.error("ComputerBlockEntity not found at position: {}", pos);
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("mcjsmod.networking.failed", e.getMessage()));
            return null;
        });
    }

    private static void sendJavaScriptResult(ServerPlayer player, BlockPos pos, String output, Object result) {
        JavaScriptResultPayload resultPayload = new JavaScriptResultPayload(pos, output, result != null ? result.toString() : "null");

        // Get the chunk position of the block entity
        ChunkPos chunkPos = new ChunkPos(pos);
        PacketDistributor.sendToPlayersTrackingChunk(player.serverLevel(), chunkPos, resultPayload);


    }
}