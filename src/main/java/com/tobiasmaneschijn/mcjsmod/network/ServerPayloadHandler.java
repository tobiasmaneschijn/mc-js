package com.tobiasmaneschijn.mcjsmod.network;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.network.payload.ClientToServerInputPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {
    public static void handleClientToServerInputPayload(ClientToServerInputPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                MCJSMod.LOGGER.info("Handling client input: " + payload.input() + " at position: " + payload.pos());
                ServerLevel level = context.player().getServer().getLevel(context.player().level().dimension());
                if (level == null) {
                    MCJSMod.LOGGER.error("Server level is null for player dimension");
                    return;
                }

                BlockEntity blockEntity = level.getBlockEntity(payload.pos());
                if (blockEntity instanceof ComputerBlockEntity computerBlockEntity) {
                    MCJSMod.LOGGER.info("Providing input to ComputerBlockEntity");
                    computerBlockEntity.provideInput(payload.input());
                } else {
                    MCJSMod.LOGGER.warn("Received input for non-existent or invalid block entity at {}", payload.pos());
                }
            } catch (Exception e) {
                MCJSMod.LOGGER.error("Error handling input payload", e);
            }
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("mcjsmod.networking.failed", e.getMessage()));
            return null;
        });
    }
}
