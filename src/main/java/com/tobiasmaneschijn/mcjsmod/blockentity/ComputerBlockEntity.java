package com.tobiasmaneschijn.mcjsmod.blockentity;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.javascript.GraalJavascriptEngine;
import com.tobiasmaneschijn.mcjsmod.javascript.ModJSBindings;
import com.tobiasmaneschijn.mcjsmod.javascript.command.Command;
import com.tobiasmaneschijn.mcjsmod.javascript.command.CommandLoader;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import com.tobiasmaneschijn.mcjsmod.network.ServerUtils;
import com.tobiasmaneschijn.mcjsmod.network.payload.ComputerBlockPayload;
import com.tobiasmaneschijn.mcjsmod.network.shell.JavaScriptExecutionHandler;
import com.tobiasmaneschijn.mcjsmod.ui.screen.ComputerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.graalvm.polyglot.Context;

import java.util.Map;
import java.util.stream.Collectors;


public class ComputerBlockEntity extends BlockEntity {

    private final IJavascriptEngine javascriptEngine;
    private Map<String, Command> commands;

    private String latestResult = "";
    private String latestConsoleOutput = "";

    // Can be used to store the screen instance for this block entity, but be careful with this. It can be null.
    private ComputerScreen screen;

    public ComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, blockState);
        //check if we are on the logical server
        if (ServerUtils.isLogicalServer()) {
            //if we are on the logical server, we can create a new GraalJavascriptEngine
            javascriptEngine = new GraalJavascriptEngine();
            javascriptEngine.init();
            ModJSBindings.bindFunctions(javascriptEngine, this);
            loadCommands();
        } else {
            //if we are on the client, we can't create a new GraalJavascriptEngine
            javascriptEngine = null;
        }

    }

    public void loadCommands() {
        Map<String, Command> commands = CommandLoader.getCommands();

        // Load all command scripts into the JavaScript engine
        String allScripts = commands.values().stream()
                .map(cmd -> cmd.getScriptContent() + "\n")
                .collect(Collectors.joining());
        try {
            javascriptEngine.execute(allScripts);
        } catch (Exception e) {
            MCJSMod.LOGGER.error("Error loading command scripts", e);
        }
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

    /**
     * Clear the terminal contents of terminal screen
     */
    public void clearTerminal() {
        if (screen != null) {
            screen.setTerminalOutput("", true);
            MCJSMod.LOGGER.info("Terminal cleared");
            // Clear the terminal contents in the JavaScript engine
            if (javascriptEngine != null) {
                javascriptEngine.clearTerminal();
            }
        }
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos(worldPosition);
    }

    public void setScreen(ComputerScreen screen) {
        this.screen = screen;
    }

    public ComputerScreen getScreen() {
        return screen;
    }

}