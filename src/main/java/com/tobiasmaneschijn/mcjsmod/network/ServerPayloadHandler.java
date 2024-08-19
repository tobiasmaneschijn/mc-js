package com.tobiasmaneschijn.mcjsmod.network;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.network.payload.ComputerBlockPayload;
import com.tobiasmaneschijn.mcjsmod.network.payload.RunComputerCodePayload;
import com.tobiasmaneschijn.mcjsmod.network.shell.JavaScriptExecutionHandler;
import com.tobiasmaneschijn.mcjsmod.network.shell.JavaScriptExecutionPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleJavaScriptExecutionPayload(JavaScriptExecutionPayload payload, IPayloadContext context) {
        JavaScriptExecutionHandler.handleJavaScriptExecutionPayload(payload, context);
    }


}