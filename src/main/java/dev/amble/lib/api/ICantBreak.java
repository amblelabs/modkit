package dev.amble.lib.api;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * An interface which stops a block from being broken.
 */
public interface ICantBreak {
    /**
     * Called when the block was attempted to be broken.
     * This exists for backwards compatibility.
     */
    default void onTryBreak(World world, BlockPos pos, BlockState state) {
        this.onTryBreak(world, pos, state, null);
    }

    /**
     * Called when the block was attempted to be broken but also includes the player who attempts to break it.
     *
     * @param player The player who attempted to break the block, or null if not available.
     */
    default void onTryBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        this.onTryBreak(world, pos, state);
    }
}
