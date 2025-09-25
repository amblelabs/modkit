package dev.amble.lib.animation;

import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface SoundProvider {
	World getWorld();
	boolean isSilent();
	SoundCategory getSoundCategory();
	Vec3d getSoundPosition();
}
