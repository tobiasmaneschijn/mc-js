package com.tobiasmaneschijn.mcjsmod.network;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.network.payload.ServerToClientOutputPayload;
import com.tobiasmaneschijn.mcjsmod.ui.screen.ComputerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleServerToClientOutputPayload(ServerToClientOutputPayload payload, IPayloadContext context) {
        MCJSMod.LOGGER.info("Received ServerToClientOutputPayload: " + payload.output());
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof ComputerScreen computerScreen && computerScreen.getBlockPos().equals(payload.pos())) {
                MCJSMod.LOGGER.info("Adding output to ComputerScreen: " + payload.output());
                computerScreen.addOutput(payload.output(), payload.isError());
            } else {
                MCJSMod.LOGGER.info("ComputerScreen not open, storing output in ComputerBlockEntity");
                if (minecraft.level != null) {
                    BlockEntity blockEntity = minecraft.level.getBlockEntity(payload.pos());
                    if (blockEntity instanceof ComputerBlockEntity computerBlockEntity) {
                        computerBlockEntity.addOutput(payload.output(), payload.isError());
                    }
                }
            }
        }).exceptionally(e -> {
            MCJSMod.LOGGER.error("Error handling ServerToClientOutputPayload", e);
            context.disconnect(Component.translatable("mcjsmod.networking.failed", e.getMessage()));
            return null;
        });
    }
}
