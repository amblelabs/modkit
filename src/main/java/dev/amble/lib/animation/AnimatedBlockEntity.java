package dev.amble.lib.animation;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * An interface for block entities that can have animations.
 * Provides a default implementation for generating a UUID based on the block entity's position and world.
 * Note: This implementation assumes that the block entity is in a loaded world.
 * getAge should be implemented with a private int age field incremented each tick in the block entity's tick method.
 */
public interface AnimatedBlockEntity extends AnimatedInstance {
	@Override
	default UUID getUuid() {
		if (!(this instanceof BlockEntity be)) {
			throw new UnsupportedOperationException("getUuid() is only supported for BlockEntity instances. Override this method");
		}

		BlockPos pos = be.getPos();
		return new UUID(be.getWorld().getRegistryKey().getValue().hashCode(), pos.asLong());
	}

	@Override
	default World getWorld() {
		if (!(this instanceof BlockEntity be)) {
			throw new UnsupportedOperationException("getWorld() is only supported for BlockEntity instances. Override this method");
		}

		return be.getWorld();
	}

	@Override
	default boolean isSilent() {
		return false;
	}

	@Override
	default SoundCategory getSoundCategory() {
		return SoundCategory.BLOCKS;
	}

	@Override
	default Vec3d getEffectPosition(float tickDelta) {
		if (!(this instanceof BlockEntity be)) {
			throw new UnsupportedOperationException("getSoundPosition() is only supported for BlockEntity instances. Override this method");
		}

		return Vec3d.ofCenter(be.getPos());
	}

	@Override
	default float getHeadYaw() {
		return 0;
	}

	@Override
	default float getBodyYaw() {
		return 0;
	}

	@Override
	default float getPitch() {
		return 0;
	}
}
