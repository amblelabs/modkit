package dev.amble.lib.mixin.client;

import dev.amble.lib.skin.SkinData;
import dev.amble.lib.skin.SkinTracker;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerMixin {
	@Inject(method="getSkinTexture", at=@At("HEAD"), cancellable = true)
	private void amblekit$getSkinTexture(CallbackInfoReturnable<Identifier> cir) {
		AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)(Object)this;

		SkinTracker tracker = SkinTracker.getInstance();

		SkinData data = tracker.get(player.getUuid());
		if (data == null) return;

		Identifier id = data.get();
		if (id == null) return;

		cir.setReturnValue(id);
	}

	@Inject(method="getModel", at=@At("HEAD"), cancellable = true)
	private void amblekit$getModel(CallbackInfoReturnable<String> cir) {
		AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;

		SkinTracker tracker = SkinTracker.getInstance();

		SkinData data = tracker.get(player.getUuid());
		if (data == null) return;

		cir.setReturnValue(data.slim() ? "slim" : "default");
	}
}
