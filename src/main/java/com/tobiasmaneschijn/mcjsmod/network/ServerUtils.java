package com.tobiasmaneschijn.mcjsmod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;


public class ServerUtils {

    public static boolean isLogicalServer() {
        if (Minecraft.getInstance().level != null) {
            return !Minecraft.getInstance().level.isClientSide;
        }
        return false;
    }

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }


}

