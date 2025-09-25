package dev.amble.lib.animation;

import dev.amble.lib.client.bedrock.BedrockModelReference;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface BedrockModelProvider {
	@Nullable
	default BedrockModelReference getModel() {
		return null;
	}

	@Nullable
	default Identifier getTexture() {
		if (getModel() == null) return null;

		BedrockModelReference model = getModel();
		Identifier id = model.id();

		String prefix = getTexturePrefix();
		if (!prefix.isEmpty() && !prefix.endsWith("/")) {
			prefix += "/";
		}

		String namespace = getModId();
		if (namespace.isEmpty()) {
			namespace = id.getNamespace();
		}

		return Identifier.of(namespace, "textures/" + prefix + model.id().getPath() + ".png");
	}

	@Nullable
	default Identifier getEmissionTexture() {
		if (!hasEmission()) return null;

		Identifier texture = getTexture();
		if (texture == null) return null;

		// add _emission suffix
		return Identifier.of(texture.getNamespace(), texture.getPath().replace(".png", "_emission.png"));
	}

	default boolean hasEmission() {
		return false;
	}

	default String getModId() {
		return "";
	}

	default String getTexturePrefix() {
		return "";
	}
}
