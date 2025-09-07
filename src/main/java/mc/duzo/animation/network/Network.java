package mc.duzo.animation.network;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.network.ServerPlayerEntity;

public class Network {
    public static void toTracking(FabricPacket packet, ServerPlayerEntity target) {
        ServerPlayNetworking.send(target, packet);
        PlayerLookup.tracking(target).forEach(p -> ServerPlayNetworking.send(p, packet));
    }
}
