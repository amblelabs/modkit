package dev.amble.lib.animation;

import dev.amble.lib.AmbleKit;
import dev.amble.lib.client.bedrock.BedrockAnimationReference;
import dev.amble.lib.client.bedrock.BedrockAnimationRegistry;
import dev.amble.lib.util.ServerLifecycleHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.entity.EntityLike;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnimationTracker {
	private static final AnimationTracker INSTANCE = new AnimationTracker();
	public static final Identifier SYNC_KEY = AmbleKit.id("animation_sync");

	public static AnimationTracker getInstance() {
		return INSTANCE;
	}

	public static void init() {
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

	private final HashMap<UUID, BedrockAnimationReference> animations = new HashMap<>();
	private final Set<UUID> updated = new HashSet<>(); // entities which have been updated recently

	@Nullable
	public BedrockAnimationReference get(EntityLike entity) {
		if (!(entity instanceof AnimatedEntity)) return null;

		return this.animations.get(entity.getUuid());
	}

	public void add(UUID id, BedrockAnimationReference animation) {
		this.animations.put(id, animation);

		sync(toBuf(id, animation));
	}

	public void add(EntityLike entity, BedrockAnimationReference animation) {
		if (!(entity instanceof AnimatedEntity)) return;

		this.add(entity.getUuid(), animation);
	}

	public void remove(UUID id) {
		this.animations.remove(id);

		sync(toRemovalBuf(id));
	}

	public void remove(EntityLike entity) {
		if (!(entity instanceof AnimatedEntity)) return;

		this.remove(entity.getUuid());
	}

	public boolean isDirty(EntityLike entity) {
		if (!(entity instanceof AnimatedEntity)) return false;

		return this.updated.remove(entity.getUuid());
	}

	private void clear() {
		this.animations.clear();
	}

	private PacketByteBuf toBuf(Map<UUID, BedrockAnimationReference> map) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeInt(map.size());
		for (Map.Entry<UUID, BedrockAnimationReference> entry : map.entrySet()) {
			buf.writeUuid(entry.getKey());
			buf.writeIdentifier(entry.getValue().id());
		}

		return buf;
	}

	private PacketByteBuf toBuf() {
		return toBuf(this.animations);
	}

	private PacketByteBuf toBuf(UUID id, BedrockAnimationReference animation) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeInt(1);
		buf.writeUuid(id);
		buf.writeIdentifier(animation.id());

		return buf;
	}

	private PacketByteBuf toRemovalBuf(UUID id) {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeInt(-1);
		buf.writeUuid(id);

		return buf;
	}

	private void receive(PacketByteBuf buf) {
		int count = buf.readInt();

		if (count == -1) {
			UUID id = buf.readUuid();
			this.animations.remove(id);
			return;
		}

		for (int i = 0; i < count; i++) {
			UUID id = buf.readUuid();
			BedrockAnimationReference reference = BedrockAnimationReference.parse(buf.readIdentifier());

			this.animations.put(id, reference);
			this.updated.add(id);
		}
	}

	public void sync() {
		sync(toBuf());
	}

	public void sync(ServerPlayerEntity target) {
		sync(toBuf(), target);
	}

	private void sync(PacketByteBuf buf) {
		ServerLifecycleHooks.get().getPlayerManager().getPlayerList().forEach((p) -> this.sync(buf, p));
	}

	private void sync(PacketByteBuf buf, ServerPlayerEntity player) {
		ServerPlayNetworking.send(player, SYNC_KEY, buf);
	}
}
