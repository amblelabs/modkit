package dev.amble.lib.client.bedrock;

import com.mojang.serialization.Codec;
import dev.amble.lib.api.Identifiable;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record BedrockAnimationReference(String fileName, String animationName) implements Identifiable {
	public static Codec<BedrockAnimationReference> CODEC = Identifier.CODEC.xmap(
			BedrockAnimationReference::parse,
			BedrockAnimationReference::id
	);

	@Override
	public Identifier id() {
		return Identifier.of(fileName, animationName);
	}

	public Optional<BedrockAnimation> get() {
		BedrockAnimation animation = BedrockAnimationRegistry.getInstance().get(this);
		return Optional.ofNullable(animation);
	}

	public static BedrockAnimationReference parse(Identifier id) {
		return new BedrockAnimationReference(id.getNamespace(), id.getPath());
	}
}
