package dev.amble.lib.mixin;

import dev.amble.lib.animation.AnimatedEntity;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SkeletonEntity.class)
public abstract class SkeletonEntityMixin extends HostileEntity implements AnimatedEntity {
	private SkeletonEntityMixin(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
	}


	private AnimationState amblekit$animationState = new AnimationState();
	@Override
	public AnimationState getAnimationState() {
		return amblekit$animationState;
	}
}