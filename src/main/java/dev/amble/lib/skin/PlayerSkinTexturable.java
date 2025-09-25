package dev.amble.lib.skin;

import dev.amble.lib.skin.client.SkinGrabber;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.world.entity.EntityLike;

public interface PlayerSkinTexturable extends EntityLike {
	default SkinData getSkin() {
		return SkinTracker.getInstance().get(this.getUuid());
	}

	default void setSkin(SkinData skin) {
		skin.upload(this);
	}

	@Environment(EnvType.CLIENT)
	default Identifier getSkinTexture() {
		SkinData skin = this.getSkin();
		if (skin == null) return SkinGrabber.missing();
		return skin.get();
	}
}
