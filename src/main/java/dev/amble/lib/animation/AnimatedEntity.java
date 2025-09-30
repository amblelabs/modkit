package dev.amble.lib.animation;

import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import dev.amble.lib.client.bedrock.BedrockModel;
import dev.amble.lib.client.bedrock.BedrockModelReference;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import org.jetbrains.annotations.Nullable;

public interface AnimatedEntity extends EntityLike, AnimatedInstance {
	@Override
	default int getAge() {
		if (this instanceof Entity entity) {
			return entity.age;
		}

		throw new UnsupportedOperationException("getAge() is only supported for Entity instances. Override this method");
	}

	@Nullable
	static AnimatedEntity getInstance(EntityLike entity) {
		if (entity instanceof AnimatedEntity animated) {
			return animated;
		}
		return null;
	}

	@Override
	default World getWorld() {
		if (!(this instanceof Entity be)) {
			throw new UnsupportedOperationException("getWorld() is only supported for Entity instances. Override this method");
		}

		return be.getWorld();
	}

	@Override
	default boolean isSilent() {
		if (!(this instanceof Entity be)) {
			throw new UnsupportedOperationException("isSilent() is only supported for Entity instances. Override this method");
		}

		return be.isSilent();
	}

	@Override
	default SoundCategory getSoundCategory() {
		if (!(this instanceof Entity be)) {
			throw new UnsupportedOperationException("getSoundCategory() is only supported for Entity instances. Override this method");
		}

		return be.getSoundCategory();
	}

	@Override
	default Vec3d getEffectPosition(float tickDelta) {
		if (!(this instanceof Entity entity)) throw new UnsupportedOperationException("getEffectPosition() is only supported for Entity instances. Override this method");

		return new Vec3d(
				MathHelper.lerp(tickDelta, entity.prevX, entity.getX()),
				MathHelper.lerp(tickDelta, entity.prevY, entity.getY()),
				MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ()));
	}
}
