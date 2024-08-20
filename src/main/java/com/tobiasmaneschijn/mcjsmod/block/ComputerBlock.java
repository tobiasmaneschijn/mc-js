package com.tobiasmaneschijn.mcjsmod.block;

import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.ui.screen.ComputerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.datafix.fixes.ChunkPalettedStorageFix;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;

public class ComputerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ComputerBlock(Properties properties) {
        super(properties);

        // can't be exploded
        properties.explosionResistance(6000000.0F);

        // can't be moved by pistons
        properties.pushReaction(PushReaction.BLOCK);

        // easy to break
        properties.strength(0.5F);

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        // open screen
        if(blockEntity instanceof ComputerBlockEntity) {
            Minecraft.getInstance().setScreen(new ComputerScreen(pos));
            ((ComputerBlockEntity) blockEntity).setScreen((ComputerScreen) Minecraft.getInstance().screen);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ComputerBlockEntity(blockPos, blockState);
    }
}
