package com.tobiasmaneschijn.mcjsmod.blockentity;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.javascript.GraalJavascriptEngine;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public class ComputerBlockEntity extends BlockEntity {

    private final IJavascriptEngine javascriptEngine;

    private String latestResult = "";
    private String latestConsoleOutput = "";

    private String code = "";
    private String latestError = "";

    public ComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, blockState);
        javascriptEngine = new GraalJavascriptEngine();
        javascriptEngine.init();

    }


    public IJavascriptEngine getJavascriptEngine() {
        return javascriptEngine;
    }

    public void saveEngineState(String state) {
        // Implement logic to save the state, possibly to NBT
    }

    public String loadEngineState() {
        // Implement logic to load the state, possibly from NBT
        return null; // Replace with actual implementation
    }

    public String getLatestResult() {
        return latestResult;
    }

    public void setLatestResult(String latestResult) {
        this.latestResult = latestResult;
        setChanged();
    }

    public String getLatestConsoleOutput() {
        return latestConsoleOutput;
    }

    public void setLatestConsoleOutput(String latestConsoleOutput) {
        this.latestConsoleOutput = latestConsoleOutput;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("LatestResult", latestResult);
        tag.putString("LatestConsoleOutput", latestConsoleOutput);
        // ... (save other data)
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        latestResult = tag.getString("LatestResult");
        latestConsoleOutput = tag.getString("LatestConsoleOutput");
        // ... (load other data)
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos(worldPosition);
    }
}