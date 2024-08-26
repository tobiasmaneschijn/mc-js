package com.tobiasmaneschijn.mcjsmod.network.payload;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientToServerInputPayload(BlockPos pos, String input) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientToServerInputPayload> TYPE = new CustomPacketPayload.Type<>(
             ResourceLocation.fromNamespaceAndPath(MCJSMod.MODID, "client_to_server_input_payload")
    );

    public static final StreamCodec<ByteBuf, ClientToServerInputPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ClientToServerInputPayload::pos,
            ByteBufCodecs.STRING_UTF8, ClientToServerInputPayload::input,
            ClientToServerInputPayload::new
    );

    @Override
    public CustomPacketPayload.Type<?> type() {
        return TYPE;
    }
}