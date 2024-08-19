package com.tobiasmaneschijn.mcjsmod.network;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.network.payload.ComputerBlockPayload;
import com.tobiasmaneschijn.mcjsmod.network.payload.RunComputerCodePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handleComputerBlockPayload(ComputerBlockPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {

            MinecraftServer server = context.player().getServer();
            if (server == null) {
                throw new IllegalStateException("Server is null");
            }

            ServerPlayer player = server.getPlayerList().getPlayer(context.player().getUUID());

            if (player != null) {
                ServerLevel level = player.serverLevel();
                BlockEntity blockEntity = level.getBlockEntity(payload.pos());
                if (blockEntity instanceof ComputerBlockEntity computerBlockEntity) {
                    computerBlockEntity.setCode(payload.code());
                    // Optionally, you can broadcast the update to other clients
                    // broadcastCodeUpdate(computerBlockEntity, level);
                }
            }
        }).exceptionally(e -> {
            // Handle exception
            context.disconnect(Component.translatable("mcjsmod.networking.failed", e.getMessage()));
            return null;
        });
    }

    public static void handleRunComputerCodePayload(RunComputerCodePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            MinecraftServer server = context.player().getServer();
            if (server == null) {
                throw new IllegalStateException("Server is null");
            }
            ServerPlayer player = server.getPlayerList().getPlayer(context.player().getUUID());
            if (player != null) {
                ServerLevel level = player.serverLevel();
                BlockEntity blockEntity = level.getBlockEntity(payload.pos());
                if (blockEntity instanceof ComputerBlockEntity computerBlockEntity) {
                    computerBlockEntity.executeCode(player);
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("mcjsmod.networking.failed", e.getMessage()));
            return null;
        });
    }

    private static void broadcastCodeUpdate(ComputerBlockEntity blockEntity, ServerLevel level) {
        ComputerBlockPayload payload = new ComputerBlockPayload(blockEntity.getBlockPos(), blockEntity.getCode());
        PacketDistributor.sendToPlayersTrackingChunk(level, blockEntity.getChunkPos(), payload);
    }

}