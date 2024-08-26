package com.tobiasmaneschijn.mcjsmod.network.payload;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerToClientOutputPayload(BlockPos pos, String output, boolean isError) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerToClientOutputPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MCJSMod.MODID, "server_to_client_output_payload")
    );

    public static final StreamCodec<ByteBuf, ServerToClientOutputPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerToClientOutputPayload::pos,
            ByteBufCodecs.STRING_UTF8, ServerToClientOutputPayload::output,
            ByteBufCodecs.BOOL, ServerToClientOutputPayload::isError,
            ServerToClientOutputPayload::new
    );


    @Override
    public CustomPacketPayload.Type<?> type() {
        return TYPE;
    }
}

