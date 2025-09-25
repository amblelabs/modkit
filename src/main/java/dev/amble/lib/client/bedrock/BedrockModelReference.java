package dev.amble.lib.client.bedrock;

import com.mojang.serialization.Codec;
import dev.amble.lib.api.Identifiable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record BedrockModelReference(String fileName, String animationName) implements Identifiable {
	public static Codec<BedrockModelReference> CODEC = Identifier.CODEC.xmap(
			BedrockModelReference::parse,
			BedrockModelReference::id
	);

	@Override
	public Identifier id() {
		return Identifier.of(fileName, animationName);
	}

	@Environment(EnvType.CLIENT)
	public Optional<BedrockModel> get() {
		BedrockModel animation = BedrockModelRegistry.getInstance().get(this.id());
		return Optional.ofNullable(animation);
	}

	public static BedrockModelReference parse(Identifier id) {
		return new BedrockModelReference(id.getNamespace(), id.getPath());
	}
}
