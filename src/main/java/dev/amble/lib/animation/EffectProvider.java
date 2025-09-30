package dev.amble.lib.animation;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface EffectProvider {
	World getWorld();
	boolean isSilent();
	SoundCategory getSoundCategory();
	float getHeadYaw();
	float getBodyYaw();
	Vec3d getEffectPosition(float tickDelta);
}
