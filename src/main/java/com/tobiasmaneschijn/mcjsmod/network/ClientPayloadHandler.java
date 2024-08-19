package com.tobiasmaneschijn.mcjsmod.network;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.network.payload.ComputerBlockPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleComputerBlockPayload(ComputerBlockPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Update client-side code block entity
            // You might need to get the block entity from the world using a position sent in the payload
            ComputerBlockEntity blockEntity = null; // Get your block entity somehow

            BlockEntity be = null;
            if (Minecraft.getInstance().level != null) {
                be = Minecraft.getInstance().level.getBlockEntity(payload.pos());
            }
            if (be instanceof ComputerBlockEntity) {
                blockEntity = (ComputerBlockEntity) be;
                blockEntity.setCode(payload.code());
                MCJSMod.LOGGER.debug("Received code update for ComputerBlockEntity at {} with code {}", payload.pos(), payload.code());
            }

        }).exceptionally(e -> {
            // Handle exception
            context.disconnect(Component.translatable("mcjsmod.networking.failed", e.getMessage()));
            return null;
        });
    }
}
