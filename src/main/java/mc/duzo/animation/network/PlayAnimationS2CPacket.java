package mc.duzo.animation.network;

import java.util.UUID;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import mc.duzo.animation.DuzoAnimationMod;
import mc.duzo.animation.generic.AnimationHolder;
import mc.duzo.animation.generic.AnimationTracker;
import mc.duzo.animation.registry.AnimationRegistry;
import mc.duzo.animation.registry.client.TrackerRegistry;

public record PlayAnimationS2CPacket(UUID player, Identifier tracker, Identifier animation) implements FabricPacket {
    public static final PacketType<PlayAnimationS2CPacket> TYPE = PacketType.create(new Identifier(DuzoAnimationMod.MOD_ID, "play_animation"), PlayAnimationS2CPacket::new);

    public PlayAnimationS2CPacket(PacketByteBuf buf) {
        this(buf.readUuid(), new Identifier(buf.readString()), new Identifier(buf.readString()));
    }
    public PlayAnimationS2CPacket(ServerPlayerEntity player, AnimationTracker tracker, AnimationHolder animation) {
        this(player.getUuid(), tracker.id(), animation.id());
    }
    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(player);
        buf.writeString(tracker.toString());
        buf.writeString(animation.toString());
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public void handle(ClientPlayerEntity client, PacketSender sender) {
        AnimationTracker tracker = TrackerRegistry.REGISTRY.get(tracker());
        if (tracker == null) return;

        Supplier<AnimationHolder> supplier = (Supplier<AnimationHolder>) AnimationRegistry.instance().REGISTRY.get(animation());
        if (supplier == null) return;

        tracker.add(player(), supplier.get());
    }
}
