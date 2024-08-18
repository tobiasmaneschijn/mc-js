package com.tobiasmaneschijn.mcjsmod.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("mcjsmod");

    public static final DeferredBlock<ComputerBlock> COMPUTER_BLOCK = BLOCKS.registerBlock("computer_block", ComputerBlock::new, BlockBehaviour.Properties.of());

}
