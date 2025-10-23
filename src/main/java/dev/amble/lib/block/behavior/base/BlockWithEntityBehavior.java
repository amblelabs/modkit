package dev.amble.lib.block.behavior.base;

import dev.amble.lib.block.behavior.InvisibleBlockBehavior;
import dev.amble.lib.block.behavior.api.Archetype;
import dev.amble.lib.block.behavior.api.BlockBehavior;
import dev.amble.lib.block.behavior.api.BlockBehaviors;
import dev.amble.lib.blockentity.ABlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class BlockWithEntityBehavior implements BlockBehavior {

    private final BiFunction<BlockPos, BlockState, ? extends BlockEntity> func;

    public BlockWithEntityBehavior(BiFunction<BlockPos, BlockState, ? extends BlockEntity> func) {
        this.func = func;
    }

    @Override
    public void init(Block block) {
        if (!(block instanceof BlockEntityProvider))
            throw new IllegalStateException("Block " + block.getClass() + " does not implement a block entity provider!");
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return func.apply(pos, state);
    }

    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public int idx() {
        return BlockBehaviors.BLOCK_WITH_ENTITY;
    }

    public static class Ticking extends BlockWithEntityBehavior {

        public Ticking(BiFunction<BlockPos, BlockState, ? extends ABlockEntity> func) {
            super(func);
        }

        @Override
        public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
            return ABlockEntity::tick;
        }

        public static Archetype withInvisibleModel(BiFunction<BlockPos, BlockState, ? extends ABlockEntity> func) {
            return new Archetype(new Ticking(func), InvisibleBlockBehavior.behavior);
        }
    }
}
