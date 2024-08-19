package com.tobiasmaneschijn.mcjsmod.network.shell;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record JavaScriptResultPayload(BlockPos pos, String output, String result) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JavaScriptResultPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MCJSMod.MODID, "javascript_result_payload"));

    public static final StreamCodec<ByteBuf, JavaScriptResultPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            JavaScriptResultPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            JavaScriptResultPayload::output,
            ByteBufCodecs.STRING_UTF8,
            JavaScriptResultPayload::result,
            JavaScriptResultPayload::new
    );

    @Override
    public CustomPacketPayload.Type<?> type() {
        return TYPE;
    }
}