package dev.amble.lib.skin;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SkinTracker extends HashMap<UUID, SkinData> {
	public static final Identifier SYNC_KEY = AmbleKit.id("skin_sync");

	private static SkinTracker INSTANCE;

	public static SkinTracker getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SkinTracker();
		}
		return INSTANCE;
	}

	public static void init() {
		INSTANCE = new SkinTracker();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			getInstance().sync(handler.getPlayer());
		});

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			initClient();
		}
	}

	@Environment(EnvType.CLIENT)
	private static void initClient() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			getInstance().clear();
		});

		ClientPlayNetworking.registerGlobalReceiver(SYNC_KEY, ((client, handler, buf, responseSender) -> {
			getInstance().receive(buf);
		}));
	}

	@Nullable
	public SkinData putSynced(UUID id, SkinData data) {
		SkinData previous = this.put(id, data);
		sync(toBuf(id, data));
		return previous;
	}

	@Nullable
	public SkinData removeSynced(UUID id) {
		SkinData previous = this.remove(id);
		sync(toBuf(id, SkinData.clear()));
		return previous;
	}

	public Optional<SkinData> getOptional(UUID id) {
		return Optional.ofNullable(this.get(id));
	}

	private PacketByteBuf toBuf(UUID id, SkinData data) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeInt(1);

		buf.writeUuid(id);
		data.writeBuf(buf);

		return buf;
	}

	private PacketByteBuf toBuf() {
		return toBuf(this);
	}

	private PacketByteBuf toBuf(Map<UUID, SkinData> map) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeInt(map.size());

		for (Map.Entry<UUID, SkinData> entry : map.entrySet()) {
			buf.writeUuid(entry.getKey());

			entry.getValue().writeBuf(buf);
		}

		return buf;
	}

	private void sync(PacketByteBuf buf) {
		ServerLifecycleHooks.get().getPlayerManager().getPlayerList().forEach((p) -> this.sync(buf, p));
	}

	private void sync(PacketByteBuf buf, ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, SYNC_KEY, buf);
	}

	private void receive(PacketByteBuf buf) {
		int count = buf.readInt();
		for (int i = 0; i < count; i++) {
			UUID id = buf.readUuid();
			SkinData val = SkinData.readBuf(buf);
			if (val == null) continue;
			this.put(id, val);
		}
	}

	public void sync() {
		sync(toBuf());
	}

	public void sync(ServerPlayerEntity target) {
		sync(toBuf(), target);
	}
}
