package dev.amble.lib.mixin.client;

import dev.amble.lib.duck.ModelPartDuck;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ModelPart.class)
public class ModelPartAccessor implements ModelPartDuck {
	@Shadow
	@Final
	private Map<String, ModelPart> children;

	@Override
	public Map<String, ModelPart> amblekit$getChildren() {
		return this.children;
	}
}
