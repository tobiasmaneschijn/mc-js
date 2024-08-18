package com.tobiasmaneschijn.mcjsmod.blockentity;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.block.ComputerBlock;
import com.tobiasmaneschijn.mcjsmod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MCJSMod.MODID);
    public static final Supplier<BlockEntityType<ComputerBlockEntity>> COMPUTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("computer_block_entity",  () -> BlockEntityType.Builder.of(ComputerBlockEntity::new, ModBlocks.COMPUTER_BLOCK.get()).build(null));


}
