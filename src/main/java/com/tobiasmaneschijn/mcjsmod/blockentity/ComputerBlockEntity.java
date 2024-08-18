package com.tobiasmaneschijn.mcjsmod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ComputerBlockEntity extends BlockEntity {
    private int counter = 0;

    public ComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY.get(), pos, blockState);
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("counter", counter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        counter = tag.getInt("counter");
    }

    public void incrementCounter() {
        counter++;
        setChanged();
    }

    public int getCounter() {
        return counter;
    }

}
