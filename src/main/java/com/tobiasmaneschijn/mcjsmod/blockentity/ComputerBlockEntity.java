package com.tobiasmaneschijn.mcjsmod.blockentity;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.javascript.JSEngine;
import com.tobiasmaneschijn.mcjsmod.network.ServerUtils;
import com.tobiasmaneschijn.mcjsmod.network.payload.ComputerBlockPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import net.neoforged.neoforge.network.PacketDistributor;
import com.tobiasmaneschijn.mcjsmod.network.ServerUtils;
import org.jetbrains.annotations.NotNull;

public class ComputerBlockEntity extends BlockEntity {
    private String code = "";
    private String latestResult = "";
    private String latestError = "";
    private String latestConsoleOutput = "";

    public ComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, blockState);

    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        MCJSMod.LOGGER.debug("saveAdditional called for ComputerBlockEntity at {} with code {}", worldPosition, code);
        // saving Javascript code, the latest GraalVM result, error and console output
        tag.putString("code", code);
        tag.putString("latestResult", latestResult);
        tag.putString("latestError", latestError);
        tag.putString("latestConsoleOutput", latestConsoleOutput);
        super.saveAdditional(tag, registries);

    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        MCJSMod.LOGGER.debug("loadAdditional called for ComputerBlockEntity at {} with code {}", worldPosition, code);

        //Loading Javascript code, the latest GraalVM result, error and console output
        code = tag.getString("code");
        latestResult = tag.getString("latestResult");
        latestError = tag.getString("latestError");
        latestConsoleOutput = tag.getString("latestConsoleOutput");
        super.loadAdditional(tag, registries);

    }


    // Override the method from IBlockEntityExtension
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        MCJSMod.LOGGER.debug("handleUpdateTag called for ComputerBlockEntity at {}", worldPosition);
        loadAdditional(tag, registries);
    }

    public void setCode(String code) {
        MCJSMod.LOGGER.debug("setCode called for ComputerBlockEntity at {}", worldPosition);
        this.code = code;
        if (level != null && !level.isClientSide) {
            ComputerBlockPayload payload = new ComputerBlockPayload(getBlockPos(), code);
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, getChunkPos(), payload);
        }
        setChanged();

    }

    public void setLatestResult(String latestResult) {
        this.latestResult = latestResult != null ? latestResult : "";
        setChanged();
    }

    public void setLatestError(String latestError) {
        this.latestError = latestError != null ? latestError : "";
        setChanged();
    }

    public void setLatestConsoleOutput(String latestConsoleOutput) {
        this.latestConsoleOutput = latestConsoleOutput != null ? latestConsoleOutput : "";
        setChanged();
    }

    public String getLatestResult() {
        return latestResult != null ? latestResult : "";
    }

    public String getLatestError() {
        return latestError != null ? latestError : "";
    }

    public String getLatestConsoleOutput() {
        return latestConsoleOutput != null ? latestConsoleOutput : "";
    }

    public void executeCode(ServerPlayer player) {

        if (!ServerUtils.isLogicalServer()) return;

        JSEngine engine = ServerUtils.getJSEngineManager().getEngine();
        if (engine != null) {
            try {
                ServerUtils.JSEvaluationResult result = ServerUtils.evaluateJS(code);


                setLatestError(result.errorMessage);

                Object resultValue = ServerUtils.convertJSValue(result.result);

                if (resultValue != null) {
                    setLatestResult(resultValue.toString());
                    player.sendSystemMessage(Component.literal("Return Value: " + resultValue));

                } else {
                    setLatestResult("");
                }

                setLatestConsoleOutput(result.consoleOutput);
                player.displayClientMessage(Component.literal("Console output: " + result.consoleOutput), false);


            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("Error executing code: " + e.getMessage()));
            }
        } else {
            player.sendSystemMessage(Component.literal("JavaScript engine is not available"));
        }
    }

    public String getCode() {
        return code != null ? code : "";
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos(worldPosition);
    }
}