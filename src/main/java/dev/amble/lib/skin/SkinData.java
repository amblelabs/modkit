package dev.amble.lib.skin;

import dev.amble.lib.skin.client.SkinGrabber;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record SkinData(String key, @Nullable String url) {
	public static SkinData username(String username) {
		return new SkinData(username, SkinConstants.SKIN_URL + username);
	}

	public static SkinData fromNbt(NbtCompound nbt) {
		String key = nbt.getString("Key");
		String url = nbt.contains("URL") ? nbt.getString("URL") : null;
		return new SkinData(key, url);
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		nbt.putString("Key", key);
		if (url != null) nbt.putString("URL", url);
		return nbt;
	}

	@Environment(EnvType.CLIENT)
	public Identifier get() {
		SkinGrabber grabber = SkinGrabber.INSTANCE;

		if (url == null) return grabber.getPossibleSkin(key).orElse(null);

		return grabber.getSkinOrDownload(key, url);
	}
}
