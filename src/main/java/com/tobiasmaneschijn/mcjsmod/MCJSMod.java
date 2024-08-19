package com.tobiasmaneschijn.mcjsmod;

import com.mojang.logging.LogUtils;
import com.tobiasmaneschijn.mcjsmod.block.ModBlocks;
import com.tobiasmaneschijn.mcjsmod.blockentity.ModBlockEntities;
import com.tobiasmaneschijn.mcjsmod.item.ModItems;
import com.tobiasmaneschijn.mcjsmod.javascript.JSEngineManager;
import com.tobiasmaneschijn.mcjsmod.javascript.ModJSBindings;
import com.tobiasmaneschijn.mcjsmod.network.ClientPayloadHandler;
import com.tobiasmaneschijn.mcjsmod.network.ServerPayloadHandler;
import com.tobiasmaneschijn.mcjsmod.network.ServerUtils;
import com.tobiasmaneschijn.mcjsmod.network.payload.ComputerBlockPayload;
import com.tobiasmaneschijn.mcjsmod.network.payload.RunComputerCodePayload;
import com.tobiasmaneschijn.mcjsmod.ui.menu.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import static com.tobiasmaneschijn.mcjsmod.ui.ModCreativeTabs.CREATIVE_MODE_TABS;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MCJSMod.MODID)
public class MCJSMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "mcjsmod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    private final JSEngineManager jsEngineManager;

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public MCJSMod(IEventBus modEventBus, ModContainer modContainer)
    {
        jsEngineManager = new JSEngineManager();
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // Register server events

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (MCJSMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);


        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }


    private void commonSetup(final FMLCommonSetupEvent event)
    {
        ServerUtils.setJSEngineManager(jsEngineManager);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

        if (ServerUtils.isLogicalServer()) {
            ServerUtils.setJSEngineManager(jsEngineManager);
            jsEngineManager.initializeEngine();
            System.out.println("Minecraft JS Mod Initializing: JS Engine initialized");

            if(jsEngineManager.isEngineAvailable()) {
                ModJSBindings.bindFunctions();
                System.out.println("Minecraft JS Mod Initializing: MC Functions bound");
            }

        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        if (ServerUtils.isLogicalServer()) {
            jsEngineManager.shutdownEngine();
            System.out.println("Minecraft JS Mod Stopping: JS Engine shutdown");
        }
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {

        }
        @SubscribeEvent
        public static void onRegisterScreens(RegisterMenuScreensEvent  event)
        {

        }

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            // Sets the current network version
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playBidirectional(
                    ComputerBlockPayload.TYPE,
                    ComputerBlockPayload.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleComputerBlockPayload,
                            ServerPayloadHandler::handleComputerBlockPayload
                    )
            );
            registrar.playToServer(RunComputerCodePayload.TYPE, RunComputerCodePayload.STREAM_CODEC,
                    ServerPayloadHandler::handleRunComputerCodePayload);

        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
    public static class ServerModEvents
    {

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            // Sets the current network version
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playBidirectional(
                    ComputerBlockPayload.TYPE,
                    ComputerBlockPayload.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            ClientPayloadHandler::handleComputerBlockPayload,
                            ServerPayloadHandler::handleComputerBlockPayload
                    )
            );
            registrar.playToServer(RunComputerCodePayload.TYPE, RunComputerCodePayload.STREAM_CODEC,
                    ServerPayloadHandler::handleRunComputerCodePayload);
        }

    }
}
