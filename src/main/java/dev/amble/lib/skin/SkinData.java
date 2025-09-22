package dev.amble.lib.skin;

import dev.amble.lib.skin.client.SkinGrabber;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record SkinData(String key, @Nullable String url) {
	public static SkinData username(String username) {
		return new SkinData(username, SkinConstants.SKIN_URL + username);
	}

	public static SkinData url(String url) {
		return new SkinData(SkinConstants.encodeURL(url), url);
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

	/**
	 * Uploads this skin data to the tracker for the given UUID.
	 * @param uuid the players UUID
	 */
	public void upload(UUID uuid) {
		SkinTracker.getInstance().putSynced(uuid, this);
	}

	/**
	 * Uploads this skin data to the tracker for the given player.
	 * @param player the player
	 */
	public void upload(ServerPlayerEntity player) {
		upload(player.getUuid());
	}
}
