package com.tobiasmaneschijn.mcjsmod.ui;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.block.ModBlocks;
import com.tobiasmaneschijn.mcjsmod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MCJSMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MCJSMOD_CREATIVE_TAB = CREATIVE_MODE_TABS.register("mcjsmod_creative_tab", () -> {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();

        builder.displayItems((itemDisplayParameters, output) -> {
            ModItems.ITEMS.getEntries()
                    .stream()
                    .map(DeferredHolder::get)
                    .forEach(output::accept);

            ModBlocks.BLOCKS.getEntries()
                    .stream()
                    .map(DeferredHolder::get)
                    .forEach(output::accept);
        });

        builder.icon(() -> new ItemStack(ModItems.COMPUTER_ITEM.get()));
        builder.title(Component.translatable("ui.mcjsmod.creative_tab"));

        return builder.build();
    });
}
