package dev.amble.lib.mixin;

import dev.amble.lib.animation.AnimatedEntity;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements AnimatedEntity {
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	private AnimationState amblekit$animationState = new AnimationState();

	@Override
	public AnimationState getAnimationState() {
		return amblekit$animationState;
	}
}
