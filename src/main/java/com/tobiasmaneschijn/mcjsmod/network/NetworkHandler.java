package com.tobiasmaneschijn.mcjsmod.network;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.network.payload.ClientToServerInputPayload;
import com.tobiasmaneschijn.mcjsmod.network.payload.ServerToClientOutputPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                ServerToClientOutputPayload.TYPE,
                ServerToClientOutputPayload.STREAM_CODEC,
                ClientPayloadHandler::handleServerToClientOutputPayload
        );
        registrar.playToServer(
                ClientToServerInputPayload.TYPE,
                ClientToServerInputPayload.STREAM_CODEC,
                ServerPayloadHandler::handleClientToServerInputPayload
        );
    }

    public static void sendToClient(ServerToClientOutputPayload payload, ServerPlayer player) {
        MCJSMod.LOGGER.info("Sending ServerToClientOutputPayload to player: " + player.getName().getString());
        PacketDistributor.sendToPlayersTrackingChunk(player.serverLevel(), new ChunkPos(payload.pos()), payload);
    }

    public static void sendToServer(ClientToServerInputPayload payload) {
        MCJSMod.LOGGER.info("Sending ClientToServerInputPayload to server");
        PacketDistributor.sendToServer(payload);
    }
}
