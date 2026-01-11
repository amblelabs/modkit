package dev.amble.lib.mixin.client;

import dev.amble.lib.skin.SkinData;
import dev.amble.lib.skin.SkinTracker;
import dev.amble.lib.skin.client.SkinGrabber;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin to override the player list entry skin texture to use custom skins from SkinTracker.
 * This affects the tab list (player list) head icons.
 */
@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

	@Shadow
	public abstract GameProfile getProfile();

	@Unique
	@Nullable
	private SkinData amblekit$lastSkin = null;

	/**
	 * Injects custom skin texture from SkinTracker into getSkinTexture().
	 * This affects the tab list player head icons.
	 */
	@Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
	private void amblekit$getCustomSkinTexture(CallbackInfoReturnable<Identifier> cir) {
		GameProfile profile = getProfile();
		if (profile == null) return;

		SkinTracker tracker = SkinTracker.getInstance();
		SkinData data = tracker.get(profile.getId());
		if (data == null) return;

		Identifier id = data.get();
		if (id == null) return;

		// Handle missing texture by falling back to last known skin
		if (SkinGrabber.isMissingTexture(id) && amblekit$lastSkin != null) {
			id = amblekit$lastSkin.get();
		} else {
			amblekit$lastSkin = data;
		}

		if (id != null) {
			cir.setReturnValue(id);
		}
	}

	/**
	 * Injects custom model type from SkinTracker into getModel().
	 * This affects whether the tab list shows slim or default arm model.
	 */
	@Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
	private void amblekit$getCustomModel(CallbackInfoReturnable<String> cir) {
		GameProfile profile = getProfile();
		if (profile == null) return;

		SkinTracker tracker = SkinTracker.getInstance();
		SkinData data = tracker.get(profile.getId());
		if (data == null) return;

		cir.setReturnValue(data.slim() ? "slim" : "default");
	}
}

