package dev.amble.lib.mixin;

import dev.amble.lib.animation.AnimatedEntity;
import dev.amble.lib.skin.PlayerSkinTexturable;
import dev.amble.lib.username.UsernameTracker;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements AnimatedEntity, PlayerSkinTexturable {
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	private AnimationState amblekit$animationState = new AnimationState();

	@Override
	public AnimationState getAnimationState() {
		return amblekit$animationState;
	}

	@Override
	public UUID getUuid() {
		return super.getUuid();
	}

	/**
	 * Injects custom display name from UsernameTracker into getName().
	 * This affects chat messages, death messages, and most other places where the player name is displayed.
	 */
	@Inject(method = "getName", at = @At("HEAD"), cancellable = true)
	private void amble$getCustomName(CallbackInfoReturnable<Text> cir) {
		Text customName = UsernameTracker.getInstance().get(this.getUuid());
		if (customName != null) {
			cir.setReturnValue(customName);
		}
	}

	/**
	 * Injects custom display name from UsernameTracker into getDisplayName().
	 * This affects the tab list and other UI elements that use display name.
	 */
	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	private void amble$getCustomDisplayName(CallbackInfoReturnable<Text> cir) {
		Text customName = UsernameTracker.getInstance().get(this.getUuid());
		if (customName != null) {
			cir.setReturnValue(customName);
		}
	}
}
