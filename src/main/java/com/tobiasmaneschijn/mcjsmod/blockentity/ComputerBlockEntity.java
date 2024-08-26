package com.tobiasmaneschijn.mcjsmod.blockentity;


import com.tobiasmaneschijn.mcjsmod.Config;
import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.javascript.GraalJavascriptEngine;
import com.tobiasmaneschijn.mcjsmod.javascript.ModJSBindings;
import com.tobiasmaneschijn.mcjsmod.javascript.command.Command;
import com.tobiasmaneschijn.mcjsmod.javascript.command.CommandLoader;
import com.tobiasmaneschijn.mcjsmod.javascript.filesystem.FileSystemException;
import com.tobiasmaneschijn.mcjsmod.javascript.filesystem.VirtualFileSystem;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IFileSystem;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import com.tobiasmaneschijn.mcjsmod.network.NetworkHandler;
import com.tobiasmaneschijn.mcjsmod.network.ServerUtils;
import com.tobiasmaneschijn.mcjsmod.network.payload.ServerToClientOutputPayload;
import com.tobiasmaneschijn.mcjsmod.ui.screen.ComputerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ComputerBlockEntity extends BlockEntity {


    private IFileSystem fileSystem;
    private IJavascriptEngine javascriptEngine;
    private final List<String> outputBuffer;
    private String latestResult = "";
    private String latestConsoleOutput = "";
    private ComputerScreen screen;
    private boolean isOSInitialized = false;
    private CompletableFuture<String> pendingInput;

    private static final int MAX_OUTPUT_BUFFER_SIZE = 1000; // Adjust as needed

    private boolean hasNewOutput = false;

    public ComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, blockState);
        this.outputBuffer = new ArrayList<>();

    }

    public CompletableFuture<String> getPendingInput() {
        return pendingInput;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            this.fileSystem = new VirtualFileSystem();
            this.javascriptEngine = new GraalJavascriptEngine();

            try {
                MCJSMod.LOGGER.info("Initializing ComputerBlockEntity");
                this.javascriptEngine.initialize();
                ModJSBindings.bindFunctions(javascriptEngine, this);
                MCJSMod.LOGGER.info("ComputerBlockEntity initialized successfully");

                if (!isOSInitialized) {
                    initializeOS();
                }
            } catch (Exception e) {
                MCJSMod.LOGGER.error("Error initializing ComputerBlockEntity", e);
            }
        }
        else {
            MCJSMod.LOGGER.error("ComputerBlockEntity not initialized, not on logical server");
        }
    }

    private boolean shouldClearInput = false;

    public void signalClearInput() {
        this.shouldClearInput = true;
    }

    public boolean shouldClearInput() {
        if (shouldClearInput) {
            shouldClearInput = false;
            return true;
        }
        return false;
    }

    private void initializeOS() {


        if (javascriptEngine != null && !isOSInitialized && level != null && !level.isClientSide) {
            try {
                String bootloaderScript = loadResourceScript("/data/mcjsmod/js/bootloader.js");
                javascriptEngine.loadScript(bootloaderScript);

                try {
                    javascriptEngine.executeScript();
                } catch (Exception e) {
                    MCJSMod.LOGGER.error("Error executing bootloader script", e);
                }

                isOSInitialized = true;
                MCJSMod.LOGGER.info("OS initialized successfully");
            } catch (Exception e) {
                MCJSMod.LOGGER.error("Error initializing OS", e);
            }
        } else {
            MCJSMod.LOGGER.error("OS already initialized or not on logical server");
        }
    }
    private String loadResourceScript(String resourcePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                MCJSMod.LOGGER.error("Failed to load script: " + resourcePath);
                return "";
            }
        } catch (Exception e) {
            MCJSMod.LOGGER.error("Error loading script: " + resourcePath, e);
            return "";
        }
    }


    public IJavascriptEngine getJavascriptEngine() {
        return javascriptEngine;
    }

    public void saveEngineState(String state) {
        // TODO: Implement state saving logic
    }

    public String loadEngineState() {
        // TODO: Implement state loading logic
        return null;
    }

    public String getLatestResult() {
        return latestResult;
    }

    public void setLatestResult(String latestResult) {
        this.latestResult = latestResult;
        setChanged();
    }

    public String getLatestConsoleOutput() {
        if (hasNewOutput) {
            hasNewOutput = false;
            return latestConsoleOutput;
        }
        return "";
    }

    public void setLatestConsoleOutput(String latestConsoleOutput) {
        this.latestConsoleOutput = latestConsoleOutput;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (fileSystem != null) {
            fileSystem.save(tag);
        }
        tag.putBoolean("isOSInitialized", isOSInitialized);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (fileSystem != null) {
            fileSystem.load(tag);
            isOSInitialized = tag.getBoolean("isOSInitialized");
            if (!isOSInitialized) {
                initializeOS();
            }
        }
    }

    public IFileSystem getFileSystem() {
        return fileSystem;
    }

    public void provideInput(String input) {
        MCJSMod.LOGGER.info("Input provided to ComputerBlockEntity: " + input);
        if (pendingInput != null && !pendingInput.isDone()) {
            MCJSMod.LOGGER.info("Completing pending input: " + input);
            pendingInput.complete(input);
        } else if (javascriptEngine != null && javascriptEngine.isRunning()) {
            MCJSMod.LOGGER.info("Passing input to JavaScript engine: " + input);
            javascriptEngine.provideInput(input);
        } else {
            MCJSMod.LOGGER.warn("Input provided, but no pending input request and JavaScript engine not running: " + input);
        }
    }


    public CompletableFuture<String> requestInput(String prompt) {
        MCJSMod.LOGGER.info("Input requested with prompt: " + prompt);
        pendingInput = new CompletableFuture<>();
        handleOutput(prompt);
        return pendingInput;
    }

    public void setPendingInput(CompletableFuture<String> pendingInput) {
        this.pendingInput = pendingInput;
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


    public void handleOutput(String output) {
        MCJSMod.LOGGER.info("Handling output in ComputerBlockEntity: " + output);
        if (level != null && !level.isClientSide) {
            ServerToClientOutputPayload payload = new ServerToClientOutputPayload(worldPosition, output, false);
            MCJSMod.LOGGER.info("Sending output to client: " + output);
            level.players().forEach(player -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.sendToClient(payload, serverPlayer);
                }
            });
        }
        outputBuffer.add(output);
        this.latestConsoleOutput = output;
        this.hasNewOutput = true;
    }



    public void addOutput(String output, boolean isError) {
        outputBuffer.add(isError ? "ERROR: " + output : output);
        while (outputBuffer.size() > MAX_OUTPUT_BUFFER_SIZE) {
            outputBuffer.remove(0);
        }
        latestConsoleOutput = isError ? "ERROR: " + output : output;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (javascriptEngine != null) {
            javascriptEngine.shutdown();
            ModJSBindings.shutdown();
        }
    }


    public List<String> getOutputBuffer() {
        return new ArrayList<>(outputBuffer);
    }
}