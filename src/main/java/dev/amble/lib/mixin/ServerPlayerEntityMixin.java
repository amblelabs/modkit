package dev.amble.lib.mixin;

import dev.amble.lib.username.UsernameTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to override the player list name (tab list display name) to use custom names from UsernameTracker.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

	/**
	 * Injects custom display name from UsernameTracker into getPlayerListName().
	 * This affects the tab list (player list) display name.
	 */
	@Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
	private void amble$getCustomPlayerListName(CallbackInfoReturnable<Text> cir) {
		ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
		Text customName = UsernameTracker.getInstance().get(self.getUuid());
		if (customName != null) {
			cir.setReturnValue(customName);
		}
	}
}

