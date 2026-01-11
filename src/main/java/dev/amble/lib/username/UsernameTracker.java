package dev.amble.lib.username;

import com.google.gson.*;
import dev.amble.lib.AmbleKit;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class UsernameTracker extends HashMap<UUID, Text> {
	public static final Identifier SYNC_KEY = AmbleKit.id("username_sync");
	private static final String CLEAR_KEY = "supersecretcodeword_clear";

	private static UsernameTracker INSTANCE;

	public static UsernameTracker getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new UsernameTracker();
		}
		return INSTANCE;
	}

	public static void init() {
		INSTANCE = new UsernameTracker();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			getInstance().sync(handler.getPlayer())
		);

		ServerLifecycleEvents.SERVER_STOPPING.register(server ->
			getInstance().write(server)
		);

		ServerLifecycleEvents.SERVER_STARTED.register(UsernameTracker::read);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			initClient();
		}
	}

	@Environment(EnvType.CLIENT)
	private static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(SYNC_KEY, (client, handler, buf, responseSender) ->
			getInstance().receive(buf)
		);
	}

	@Nullable
	public Text putSynced(UUID id, Text text) {
		Text previous = this.put(id, text);
		sync(toBuf(id, text));
		updatePlayerList(id);
		return previous;
	}

	@Nullable
	public Text removeSynced(UUID id) {
		Text previous = this.remove(id);
		sync(toBufClear(id));
		updatePlayerList(id);
		return previous;
	}

	/**
	 * Sends a player list update packet to all clients to refresh the tab list display name.
	 */
	private void updatePlayerList(UUID targetId) {
		MinecraftServer server = ServerLifecycleHooks.get();
		if (server == null) return;

		ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(targetId);
		if (targetPlayer == null) return;

		// Send UPDATE_DISPLAY_NAME action to all players to refresh the tab list
		PlayerListS2CPacket packet = new PlayerListS2CPacket(
			EnumSet.of(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME),
			Collections.singletonList(targetPlayer)
		);

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			player.networkHandler.sendPacket(packet);
		}
	}

	public Optional<Text> getOptional(UUID id) {
		return Optional.ofNullable(this.get(id));
	}

	private PacketByteBuf toBuf(UUID id, Text text) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeInt(1);
		buf.writeUuid(id);
		buf.writeString(Text.Serializer.toJson(text));

		return buf;
	}

	private PacketByteBuf toBufClear(UUID id) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeInt(1);
		buf.writeUuid(id);
		buf.writeString(CLEAR_KEY);

		return buf;
	}

	private PacketByteBuf toBuf() {
		return toBuf(this);
	}

	private PacketByteBuf toBuf(Map<UUID, Text> map) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeInt(map.size());

		for (Map.Entry<UUID, Text> entry : map.entrySet()) {
			buf.writeUuid(entry.getKey());
			buf.writeString(Text.Serializer.toJson(entry.getValue()));
		}

		return buf;
	}

	private void sync(PacketByteBuf buf) {
		ServerLifecycleHooks.get().getPlayerManager().getPlayerList().forEach(p -> this.sync(buf, p));
	}

	private void sync(PacketByteBuf buf, ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, SYNC_KEY, buf);
	}

	private void receive(PacketByteBuf buf) {
		int count = buf.readInt();
		for (int i = 0; i < count; i++) {
			UUID id = buf.readUuid();
			String json = buf.readString();

			if (json.equals(CLEAR_KEY)) {
				this.remove(id);
				continue;
			}

			Text text = Text.Serializer.fromJson(json);
			if (text != null) {
				this.put(id, text);
			}
		}
	}

	public void sync() {
		sync(toBuf());
	}

	public void sync(ServerPlayerEntity target) {
		sync(toBuf(), target);
	}

	private static Path getSavePath(MinecraftServer server) {
		return server.getSavePath(WorldSavePath.ROOT).resolve("amblekit").resolve("usernames.json");
	}

	private void write(MinecraftServer server) {
		try {
			Path savePath = getSavePath(server);
			if (!Files.exists(savePath)) {
				Files.createDirectories(savePath.getParent());
			}

			JsonObject json = new JsonObject();
			for (Map.Entry<UUID, Text> entry : this.entrySet()) {
				json.add(entry.getKey().toString(), JsonParser.parseString(Text.Serializer.toJson(entry.getValue())));
			}

			Files.writeString(savePath, AmbleKit.GSON.toJson(json));
		} catch (Exception e) {
			AmbleKit.LOGGER.error("Failed to write usernames.json", e);
		}
	}

	private static void read(MinecraftServer server) {
		if (!Files.exists(getSavePath(server))) return;

		try {
			String raw = Files.readString(getSavePath(server));
			JsonObject object = JsonParser.parseString(raw).getAsJsonObject();

			INSTANCE = new UsernameTracker();
			for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
				UUID uuid = UUID.fromString(entry.getKey());
				Text text = Text.Serializer.fromJson(entry.getValue().toString());
				if (text != null) {
					INSTANCE.put(uuid, text);
				}
			}

			INSTANCE.sync();
		} catch (Exception e) {
			AmbleKit.LOGGER.error("Failed to read usernames.json", e);
		}
	}
}

