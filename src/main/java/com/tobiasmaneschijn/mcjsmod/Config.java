package com.tobiasmaneschijn.mcjsmod;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = MCJSMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();


    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    public static ModConfigSpec.IntValue MAX_CONCURRENT_SCRIPTS = BUILDER
            .comment("Maximum number of scripts that can run concurrently")
            .defineInRange("maxConcurrentScripts", 5, 1, 20);
    public static ModConfigSpec.LongValue MAX_SCRIPT_EXECUTION_TIME = BUILDER
            .comment("Maximum execution time for a script in milliseconds")
            .defineInRange("maxScriptExecutionTime", 5000L, 1000L, 30000L);
    public static ModConfigSpec.LongValue MAX_MEMORY_USAGE = BUILDER
            .comment("Maximum memory usage for all scripts in bytes")
            .defineInRange("maxMemoryUsage", 100 * 1024 * 1024L, 10 * 1024 * 1024L, 1024 * 1024 * 1024L);
    public static ModConfigSpec.IntValue MAX_CONTEXTS_PER_PLAYER = BUILDER
            .comment("Maximum number of script contexts a player can have")
            .defineInRange("maxContextsPerPlayer", 3, 1, 10);


    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        MAX_CONCURRENT_SCRIPTS.get();
        MAX_SCRIPT_EXECUTION_TIME.get();
        MAX_MEMORY_USAGE.get();
        MAX_CONTEXTS_PER_PLAYER.get();

        MCJSMod.LOGGER.debug("Loaded MCJSMod config file {}", event.getConfig().getFileName());
    }
}
