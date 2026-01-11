package dev.amble.lib.mixin.client;

import dev.amble.lib.username.UsernameTracker;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
	@ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private Text amble$modifyLabelText(Text original, T entity) {
		Text customName = UsernameTracker.getInstance().get(entity.getUuid());
		if (customName == null) return original;

		return customName;
	}
}

