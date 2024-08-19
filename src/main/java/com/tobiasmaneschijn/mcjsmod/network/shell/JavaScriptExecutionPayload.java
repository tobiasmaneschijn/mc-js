package com.tobiasmaneschijn.mcjsmod.network.shell;


import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record JavaScriptExecutionPayload(BlockPos pos, String script) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JavaScriptExecutionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MCJSMod.MODID, "javascript_execution_payload"));

    public static final StreamCodec<ByteBuf, JavaScriptExecutionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            JavaScriptExecutionPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            JavaScriptExecutionPayload::script,
            JavaScriptExecutionPayload::new
    );

    @Override
    public CustomPacketPayload.Type<?> type() {
        return TYPE;
    }
}
