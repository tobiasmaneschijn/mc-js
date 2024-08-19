package com.tobiasmaneschijn.mcjsmod.javascript;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.javascript.interfaces.IJavascriptEngine;
import com.tobiasmaneschijn.mcjsmod.network.ServerUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;

public class JSEngineManager {
    private IJavascriptEngine jsEngine;

    public void initializeEngine() {
        if (ServerUtils.isLogicalServer()) {
            jsEngine = new GraalJavascriptEngine();

            // Bind Minecraft-specific functions
            jsEngine.bindFunction("placeBlock", (args) -> {
                // Implementation to place a block in Minecraft
                System.out.println("Placing block at: " + args[0] + ", " + args[1] + ", " + args[2]);
                return null;
            });

            System.out.println("JS Engine initialized on server start");
        }
    }

    public void shutdownEngine() {
        if (jsEngine != null) {
            jsEngine.close();
            jsEngine = null;
            System.out.println("JS Engine closed on server stop");
        }
    }

    public IJavascriptEngine getEngine() {
        return jsEngine;
    }

    public boolean isEngineAvailable() {
        return jsEngine != null;
    }
}