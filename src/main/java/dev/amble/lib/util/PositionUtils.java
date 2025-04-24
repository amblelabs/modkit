package dev.amble.lib.util;

import dev.amble.lib.data.DirectedBlockPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PositionUtils {
    public static Vec3d offsetPos(DirectedBlockPos directed, float value) {
        BlockPos pos = directed.getPos();

        return new Vec3d(
                pos.getX() + value * directed.getVector().getX(),
                pos.getY() + value * directed.getVector().getY(),
                pos.getZ() + value * directed.getVector().getZ()
        );
    }
}

