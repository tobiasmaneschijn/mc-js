package com.tobiasmaneschijn.mcjsmod.network.payload;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

public record ComputerBlockPayload(BlockPos pos, String code) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComputerBlockPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MCJSMod.MODID, "computer_block_payload"));

    public static final StreamCodec<ByteBuf, ComputerBlockPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ComputerBlockPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            ComputerBlockPayload::code,
            ComputerBlockPayload::new
    );

    @Override
    public CustomPacketPayload.Type<?> type() {
        return TYPE;
    }
}