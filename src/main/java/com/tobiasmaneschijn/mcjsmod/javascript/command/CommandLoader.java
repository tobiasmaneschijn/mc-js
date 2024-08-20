package com.tobiasmaneschijn.mcjsmod.javascript.command;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.tobiasmaneschijn.mcjsmod.MCJSMod.MODID;

public class CommandLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static final Map<String, Command> commands = new HashMap<>();

    public CommandLoader() {
        super(GSON, "commands");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        MCJSMod.LOGGER.info("Starting to apply commands.");
        commands.clear();
        MCJSMod.LOGGER.info("Cleared existing commands.");

        pObject.forEach((location, jsonElement) -> {
            MCJSMod.LOGGER.info("Processing command at location: " + location);
            if (jsonElement.isJsonObject()) {
                JsonObject json = jsonElement.getAsJsonObject();
                try {
                    String name = json.get("name").getAsString();
                    String description = json.get("description").getAsString();
                    MCJSMod.LOGGER.info("Found command JSON object: name=" + name + ", description=" + description);

                    String jsPath = location.getPath() + ".js";
                    ResourceLocation jsLocation =  ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "commands/" + jsPath);
                    MCJSMod.LOGGER.info("Looking for script at: " + jsLocation);

                    String scriptContent = readScriptContent(pResourceManager, jsLocation);
                    MCJSMod.LOGGER.info("Read script content for command: " + name);

                    commands.put(name, new Command(name, description, scriptContent));
                    MCJSMod.LOGGER.info("Loaded command: " + name);
                } catch (Exception e) {
                    MCJSMod.LOGGER.error("Failed to load command from " + location, e);
                }
            } else {
                MCJSMod.LOGGER.warn("Expected a JSON object but found something else at: " + location);
            }
        });

        MCJSMod.LOGGER.info("Finished applying commands. Total commands loaded: " + commands.size());
    }

    private String readScriptContent(ResourceManager resourceManager, ResourceLocation location) throws IOException {
        MCJSMod.LOGGER.info("Attempting to read script content from: " + location);
        Optional<Resource> resourceOptional = resourceManager.getResource(location);
        if (resourceOptional.isPresent()) {
            try (InputStream inputStream = resourceOptional.get().open();
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                StringBuilder content = new StringBuilder();
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    content.append(buffer, 0, read);
                }
                MCJSMod.LOGGER.info("Successfully read script content from: " + location);
                return content.toString();
            }
        } else {
            String errorMsg = "Script file not found: " + location;
            MCJSMod.LOGGER.error(errorMsg);
            throw new IOException(errorMsg);
        }
    }

    public static Map<String, Command> getCommands() {
        return commands;
    }

    public static void registerReloadListener() {
        MCJSMod.LOGGER.info("Registering CommandLoader as a reload listener.");
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.AddReloadListenerEvent event) -> {
            event.addListener(new CommandLoader());
            MCJSMod.LOGGER.info("CommandLoader has been added as a reload listener.");
        });
    }
}
