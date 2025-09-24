package dev.amble.lib.mixin;

import dev.amble.lib.animation.AnimatedEntity;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity implements AnimatedEntity {
	private ZombieEntityMixin(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
	}


	private AnimationState amblekit$animationState = new AnimationState();
	@Override
	public AnimationState getAnimationState() {
		return amblekit$animationState;
	}
}