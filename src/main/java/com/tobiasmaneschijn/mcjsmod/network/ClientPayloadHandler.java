package com.tobiasmaneschijn.mcjsmod.network;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.network.payload.ComputerBlockPayload;
import com.tobiasmaneschijn.mcjsmod.network.shell.JavaScriptResultPayload;
import com.tobiasmaneschijn.mcjsmod.ui.screen.ComputerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {


    public static void handleJavaScriptResultPayload(JavaScriptResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof ComputerScreen computerScreen) {
                computerScreen.handleJavaScriptResult(payload.output(), payload.result());
            } else {
                // If the ComputerScreen is not open, we might want to store the result
                // in the ComputerBlockEntity for later retrieval
                BlockEntity blockEntity = minecraft.level.getBlockEntity(payload.pos());
                if (blockEntity instanceof ComputerBlockEntity computerBlockEntity) {
                    computerBlockEntity.setLatestResult(payload.result());
                    computerBlockEntity.setLatestConsoleOutput(payload.output());
                }
            }
            MCJSMod.LOGGER.info("Received JavaScript execution result for block at {}: Output: {}, Result: {}",
                    payload.pos(), payload.output(), payload.result());
        }).exceptionally(e -> {
            context.disconnect(Component.translatable("mcjsmod.networking.failed", e.getMessage()));
            return null;
        });
    }
}
