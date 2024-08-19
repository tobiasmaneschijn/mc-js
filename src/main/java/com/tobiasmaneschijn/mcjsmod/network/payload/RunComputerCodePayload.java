package com.tobiasmaneschijn.mcjsmod.network.payload;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RunComputerCodePayload(BlockPos pos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RunComputerCodePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MCJSMod.MODID, "run_computer_code_payload"));

    public static final StreamCodec<ByteBuf, RunComputerCodePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            RunComputerCodePayload::pos,
            RunComputerCodePayload::new
    );

    @Override
    public @NotNull CustomPacketPayload.Type<?> type() {
        return TYPE;
    }
}