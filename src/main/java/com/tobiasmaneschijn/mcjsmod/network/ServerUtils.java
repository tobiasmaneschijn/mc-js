package com.tobiasmaneschijn.mcjsmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;


public class ServerUtils {

    public static boolean isLogicalServer() {
        return Minecraft.getInstance().isLocalServer();
    }

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }


}

